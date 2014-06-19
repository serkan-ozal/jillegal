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
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;
import tr.com.serkanozal.jillegal.util.JillegalUtil;
import tr.com.serkanozal.jillegal.util.JvmUtil;

@SuppressWarnings("restriction")
public class OffHeapUtil {
	
	private static final Logger logger = Logger.getLogger(JillegalUtil.class);
	
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
	public static <T> void injectOffHeapFields(T obj) {
		if (obj != null) {
			Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> nonPrimitiveFieldInitializers = 
					findNonPrimitiveFieldInitializers(obj.getClass());
			if (nonPrimitiveFieldInitializers != null) {
				for (NonPrimitiveFieldInitializer fieldInitializer : nonPrimitiveFieldInitializers) {
					fieldInitializer.initializeField(obj);
				}
			}
		}	
	}
	
	private static <T> Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> findNonPrimitiveFieldInitializers(Class<T> clazz) {
		synchronized (nonPrimitiveFieldInitializerMap) {
			Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> nonPrimitiveFieldInitializers = 
					nonPrimitiveFieldInitializerMap.get(clazz);
			if (nonPrimitiveFieldInitializers == null) {
				nonPrimitiveFieldInitializers = new HashSet<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>>();
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
				nonPrimitiveFieldInitializerMap.put(clazz, nonPrimitiveFieldInitializers);
			}
			return nonPrimitiveFieldInitializers;
		}
	}
	
	private static abstract class NonPrimitiveFieldInitializer<C extends OffHeapFieldConfig> {
		
		protected final sun.misc.Unsafe unsafe = JvmUtil.getUnsafe();
		@SuppressWarnings("unused")
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
		
		abstract protected void initializeField(Object obj);
		abstract protected void initializeField(long objAddress);
		
	}
	
	private static class ComplexTypedFieldInitializer extends NonPrimitiveFieldInitializer<OffHeapObjectFieldConfig> {
		
		protected ComplexTypedFieldInitializer(Field field) {
			super(field);
		}
		
		protected ComplexTypedFieldInitializer(OffHeapObjectFieldConfig fieldConfig) {
			super(fieldConfig);
			this.fieldType = 
					(fieldConfig.getFieldType() == null || fieldConfig.getFieldType().equals(Object.class)) ? 
							fieldConfig.getField().getType() : 
							fieldConfig.getFieldType();
		}
		
		protected void initializeField(Object obj) {
			jvmAwareObjectFieldInjecter.injectField(
					JvmUtil.addressOf(obj) + fieldOffset, 
					offHeapService.newObjectAsAddress(fieldType));
		}
		
		protected void initializeField(long objAddress) {
			jvmAwareObjectFieldInjecter.injectField(
					objAddress + fieldOffset, 
					offHeapService.newObjectAsAddress(fieldType));
		}
		
	}
	
	private static class ArrayTypedFieldInitializer extends NonPrimitiveFieldInitializer<OffHeapArrayFieldConfig> {
		
		protected Class<?> elementType;
		protected Class<?> arrayType;
		protected int length;
		
		protected ArrayTypedFieldInitializer(Field field) {
			super(field);
			this.elementType = field.getType().getComponentType();
			this.arrayType = field.getType();
		}
		
		protected ArrayTypedFieldInitializer(OffHeapArrayFieldConfig fieldConfig) {
			super(fieldConfig);
			this.elementType = 
					(fieldConfig.getElementType() == null || fieldConfig.getElementType().equals(Object.class)) ? 
							fieldConfig.getField().getType().getComponentType() : 
							fieldConfig.getElementType();
			this.arrayType = Array.newInstance(elementType, 0).getClass();
			this.length = fieldConfig.getLength();
		}
		
		protected void initializeField(Object obj) {
			jvmAwareObjectFieldInjecter.injectField(
					JvmUtil.addressOf(obj) + fieldOffset, 
					offHeapService.newArrayAsAddress(arrayType, length));
		}
		
		protected void initializeField(long objAddress) {
			jvmAwareObjectFieldInjecter.injectField(
					objAddress + fieldOffset, 
					offHeapService.newArrayAsAddress(arrayType, length));
		}
		
	}
	
	private interface JvmAwareObjectFieldInjecter {
		
		void injectField(long fieldAddress, long fieldObjectAddress);
	
	}
	
	private static class Address32BitJvmAwareObjectFieldInjecter implements JvmAwareObjectFieldInjecter {

		@Override
		public void injectField(long fieldAddress, long fieldObjectAddress) {
			directMemoryService.putAsIntAddress(fieldAddress, fieldObjectAddress);
		}
		
	}
	
	private static class Address64BitWithoutCompressedOopsJvmAwareObjectFieldInjecter implements JvmAwareObjectFieldInjecter {

		@Override
		public void injectField(long fieldAddress, long fieldObjectAddress) {
			directMemoryService.putLong(fieldAddress, fieldObjectAddress);
		}
		
	}
	
	private static class Address64BitWithCompressedOopsJvmAwareObjectFieldInjecter implements JvmAwareObjectFieldInjecter {

		@Override
		public void injectField(long fieldAddress, long fieldObjectAddress) {
			directMemoryService.putAsIntAddress(fieldAddress, fieldObjectAddress);
		}
		
	}
    
}
