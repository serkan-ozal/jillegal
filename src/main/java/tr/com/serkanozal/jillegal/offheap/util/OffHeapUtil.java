/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jillegal.offheap.config.OffHeapConfigService;
import tr.com.serkanozal.jillegal.offheap.config.OffHeapConfigServiceFactory;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapArrayFieldConfig;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapClassConfig;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapFieldConfig;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapObjectFieldConfig;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;
import tr.com.serkanozal.jillegal.util.JvmUtil;
import tr.com.serkanozal.jillegal.util.ReflectionUtil;

public class OffHeapUtil {
	
	private static final Logger logger = Logger.getLogger(OffHeapUtil.class);
	
	private static final OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	private static final OffHeapConfigService offHeapConfigService = OffHeapConfigServiceFactory.getOffHeapConfigService();
	private static final DirectMemoryService directMemoryService = DirectMemoryServiceFactory.getDirectMemoryService();
	private static JvmAwareObjectFieldInjecter jvmAwareObjectFieldInjecter;
	private static final Map<Class<?>, Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>>> nonPrimitiveFieldInitializerMap = 
				new HashMap<Class<?>, Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>>>();
	
	static {
		init();
	}
	
	private OffHeapUtil() {
        
    }
	
	private static void init() {
		switch (JvmUtil.getAddressSize()) {
	        case JvmUtil.SIZE_32_BIT:
	        	jvmAwareObjectFieldInjecter = new Address32BitJvmAwareObjectFieldInjecter();
	            break;
	        case JvmUtil.SIZE_64_BIT:
	        	int referenceSize = JvmUtil.getReferenceSize();
	        	switch (referenceSize) {
	             	case JvmUtil.ADDRESSING_4_BYTE:   
	             		jvmAwareObjectFieldInjecter = new Address64BitWithCompressedOopsJvmAwareObjectFieldInjecter();
	             		break;
	             	case JvmUtil.ADDRESSING_8_BYTE:
	             		jvmAwareObjectFieldInjecter = new Address64BitWithoutCompressedOopsJvmAwareObjectFieldInjecter();
	             		break;
	             	default:    
	                    throw new AssertionError("Unsupported reference size: " + referenceSize);
	        	}
	        	break;    
	        default:
	            throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());
		} 
	}
	
	@SuppressWarnings("rawtypes")
	public static <T> T injectOffHeapFields(T obj) {
		if (obj != null) {
			Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> nonPrimitiveFieldInitializers = 
					findNonPrimitiveFieldInitializers(obj.getClass());
			if (nonPrimitiveFieldInitializers != null) {
				for (NonPrimitiveFieldInitializer fieldInitializer : nonPrimitiveFieldInitializers) {
					fieldInitializer.initializeField(obj);
				}
			}
		}	
		return obj;
	}
	
	@SuppressWarnings("rawtypes")
	public static <T> long injectOffHeapFields(long objAddress, Class<T> clazz) {
		Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> nonPrimitiveFieldInitializers = 
				findNonPrimitiveFieldInitializers(clazz);
		if (nonPrimitiveFieldInitializers != null) {
			for (NonPrimitiveFieldInitializer fieldInitializer : nonPrimitiveFieldInitializers) {
				fieldInitializer.initializeField(objAddress);
			}
		}
		return objAddress;
	}
	
	@SuppressWarnings("incomplete-switch")
	private static <T> Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> findNonPrimitiveFieldInitializers(Class<T> clazz) {
		synchronized (nonPrimitiveFieldInitializerMap) {
			Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> nonPrimitiveFieldInitializers = 
					nonPrimitiveFieldInitializerMap.get(clazz);
			if (nonPrimitiveFieldInitializers == null) {
				NonPrimitiveFieldAllocationConfigType nonPrimitiveFieldAllocationConfigType = null;
				OffHeapClassConfig offHeapClassConfig = offHeapConfigService.getOffHeapClassConfig(clazz);
				if (offHeapClassConfig != null) {
					nonPrimitiveFieldAllocationConfigType = offHeapClassConfig.getNonPrimitiveFieldAllocationConfigType();
				}
				if (nonPrimitiveFieldAllocationConfigType == null) {
					nonPrimitiveFieldAllocationConfigType = NonPrimitiveFieldAllocationConfigType.getDefault();
				}
				switch (nonPrimitiveFieldAllocationConfigType) {
					case ALL_NON_PRIMITIVE_FIELDS:
						nonPrimitiveFieldInitializers = 
							findNonPrimitiveFieldInitializersForAllNonPrimitiveFields(clazz);
						break;
					case ONLY_CONFIGURED_NON_PRIMITIVE_FIELDS:
						nonPrimitiveFieldInitializers = 
							findNonPrimitiveFieldInitializersForOnlyConfiguredNonPrimitiveFields(clazz);
						break;
				}	
				if (nonPrimitiveFieldInitializers != null) {
					nonPrimitiveFieldInitializerMap.put(clazz, nonPrimitiveFieldInitializers);
				}	
			}
			return nonPrimitiveFieldInitializers;
		}
	}
	
	public static <T> Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> 
			findNonPrimitiveFieldInitializersForAllNonPrimitiveFields(Class<T> clazz) {
		Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> nonPrimitiveFieldInitializers =  
				new HashSet<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>>();
		List<Field> fields = ReflectionUtil.getAllFields(clazz);
		for (Field f : fields) {
			if (!f.getType().isPrimitive() && !f.getType().isArray()) {
				nonPrimitiveFieldInitializers.add(new ComplexTypedFieldInitializer(f));
				if (logger.isInfoEnabled()) {
					logger.info(
						"Created \"ComplexTypedFieldInitializer\" for field " + f.getName() + 
						" in class " + clazz.getName());
				}
			}		
		}
		return nonPrimitiveFieldInitializers;
	}
	
	public static <T> Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> 
			findNonPrimitiveFieldInitializersForOnlyConfiguredNonPrimitiveFields(Class<T> clazz) {
		Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> nonPrimitiveFieldInitializers = 
				new HashSet<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>>();
		OffHeapClassConfig classConfig = offHeapConfigService.getOffHeapClassConfig(clazz);
		if (classConfig != null) {
			List<OffHeapObjectFieldConfig> objectFieldConfigs = classConfig.getObjectFieldConfigs();
			if (objectFieldConfigs != null) {
				for (OffHeapObjectFieldConfig objectFieldConfig : objectFieldConfigs) {
					nonPrimitiveFieldInitializers.add(new ComplexTypedFieldInitializer(objectFieldConfig));
					if (logger.isInfoEnabled()) {
						logger.info(
							"Created \"ComplexTypedFieldInitializer\" for field " + 
							objectFieldConfig.getField().getName() + 
							" in class " + clazz.getName());
					}
				}
			}
			List<OffHeapArrayFieldConfig> arrayFieldConfigs = classConfig.getArrayFieldConfigs();
			if (arrayFieldConfigs != null) {
				for (OffHeapArrayFieldConfig arrayFieldConfig : arrayFieldConfigs) {
					nonPrimitiveFieldInitializers.add(new ArrayTypedFieldInitializer(arrayFieldConfig));
					if (logger.isInfoEnabled()) {
						logger.info(
							"Created \"ArrayTypedFieldInitializer\" for field " + 
							arrayFieldConfig.getField().getName() + 
							" in class " + clazz.getName());
					}
				}
			}
		}
		return nonPrimitiveFieldInitializers;
	}
	
	public static abstract class NonPrimitiveFieldInitializer<C extends OffHeapFieldConfig> {
		
		protected final sun.misc.Unsafe unsafe = JvmUtil.getUnsafe();
		protected C fieldConfig;
		protected final Field field;
		protected final long fieldOffset;
		protected Class<?> fieldType;
		
		protected NonPrimitiveFieldInitializer(Field field) {
			this.field = field;
			this.fieldOffset = unsafe.objectFieldOffset(field);
			this.fieldType = field.getType();
		}
		
		protected NonPrimitiveFieldInitializer(C fieldConfig) {
			this.fieldConfig = fieldConfig;
			this.field = fieldConfig.getField();
			this.fieldOffset = unsafe.objectFieldOffset(field);
			this.fieldType = field.getType();
		}

		public C getFieldConfig() {
			return fieldConfig;
		}

		public Field getField() {
			return field;
		}

		public long getFieldOffset() {
			return fieldOffset;
		}

		public Class<?> getFieldType() {
			return fieldType;
		}

		abstract public void initializeField(Object obj);
		abstract public void initializeField(long objAddress);
		
	}
	
	public static class ComplexTypedFieldInitializer extends NonPrimitiveFieldInitializer<OffHeapObjectFieldConfig> {
		
		public ComplexTypedFieldInitializer(Field field) {
			super(field);
		}
		
		public ComplexTypedFieldInitializer(OffHeapObjectFieldConfig fieldConfig) {
			super(fieldConfig);
			this.fieldType = 
					(fieldConfig.getFieldType() == null || fieldConfig.getFieldType().equals(Object.class)) ? 
							fieldConfig.getField().getType() : 
							fieldConfig.getFieldType();
		}
		
		public void initializeField(Object obj) {
			jvmAwareObjectFieldInjecter.injectField(
					JvmUtil.addressOf(obj) + fieldOffset, 
					offHeapService.newObjectAsAddress(fieldType));
		}
		
		public void initializeField(long objAddress) {
			jvmAwareObjectFieldInjecter.injectField(
					objAddress + fieldOffset, 
					offHeapService.newObjectAsAddress(fieldType));
		}
		
	}
	
	public static class ArrayTypedFieldInitializer extends NonPrimitiveFieldInitializer<OffHeapArrayFieldConfig> {
		
		protected Class<?> elementType;
		protected Class<?> arrayType;
		protected int length;

		public ArrayTypedFieldInitializer(OffHeapArrayFieldConfig fieldConfig) {
			super(fieldConfig);
			this.elementType = 
					(fieldConfig.getElementType() == null || fieldConfig.getElementType().equals(Object.class)) ? 
							fieldConfig.getField().getType().getComponentType() : 
							fieldConfig.getElementType();
			this.arrayType = Array.newInstance(elementType, 0).getClass();
			this.length = fieldConfig.getLength();
		}

		public Class<?> getElementType() {
			return elementType;
		}

		public Class<?> getArrayType() {
			return arrayType;
		}

		public int getLength() {
			return length;
		}

		public void initializeField(Object obj) {
			jvmAwareObjectFieldInjecter.injectField(
					JvmUtil.addressOf(obj) + fieldOffset, 
					offHeapService.newArrayAsAddress(arrayType, length));
		}
		
		public void initializeField(long objAddress) {
			jvmAwareObjectFieldInjecter.injectField(
					objAddress + fieldOffset, 
					offHeapService.newArrayAsAddress(arrayType, length));
		}
		
	}
	
	public interface JvmAwareObjectFieldInjecter {
		
		void injectField(long fieldAddress, long fieldObjectAddress);
	
	}
	
	public static class Address32BitJvmAwareObjectFieldInjecter implements JvmAwareObjectFieldInjecter {

		@Override
		public void injectField(long fieldAddress, long fieldObjectAddress) {
			directMemoryService.putAsIntAddress(fieldAddress, fieldObjectAddress);
		}
		
	}
	
	public static class Address64BitWithoutCompressedOopsJvmAwareObjectFieldInjecter implements JvmAwareObjectFieldInjecter {

		@Override
		public void injectField(long fieldAddress, long fieldObjectAddress) {
			directMemoryService.putLong(fieldAddress, fieldObjectAddress);
		}
		
	}
	
	public static class Address64BitWithCompressedOopsJvmAwareObjectFieldInjecter implements JvmAwareObjectFieldInjecter {

		@Override
		public void injectField(long fieldAddress, long fieldObjectAddress) {
			directMemoryService.putAsIntAddress(fieldAddress, JvmUtil.toJvmAddress(fieldObjectAddress));
		}
		
	}
    
}
