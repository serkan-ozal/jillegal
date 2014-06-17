/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jcommon.util.ReflectionUtil;
import tr.com.serkanozal.jillegal.config.ConfigManager;
import tr.com.serkanozal.jillegal.offheap.config.OffHeapConfigService;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapArrayFieldConfig;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapClassConfig;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapFieldConfig;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapObjectFieldConfig;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public abstract class BaseOffHeapPool<T, P extends OffHeapPoolCreateParameter<T>> implements OffHeapPool<T, P> {

	protected final Logger logger = Logger.getLogger(getClass());
	
	protected Class<T> elementType;
	protected DirectMemoryService directMemoryService;
	protected NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType;
	protected List<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> nonPrimitiveFieldInitializers;
	protected JvmAwareObjectFieldInjecter jvmAwareObjectFieldInjecter;
	protected OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	protected OffHeapConfigService offHeapConfigService = ConfigManager.getOffHeapConfigService();
	
	public BaseOffHeapPool(Class<T> elementType) {
		if (elementType == null) {
			throw new IllegalArgumentException("\"elementType\" cannot be null !");
		}
		this.elementType = elementType;
		this.directMemoryService = DirectMemoryServiceFactory.getDirectMemoryService();
	}
	
	public BaseOffHeapPool(Class<T> elementType, DirectMemoryService directMemoryService) {
		if (elementType == null) {
			throw new IllegalArgumentException("\"elementType\" cannot be null !");
		}
		this.elementType = elementType;
		this.directMemoryService = 
				directMemoryService != null ? 
						directMemoryService : 
						DirectMemoryServiceFactory.getDirectMemoryService();
	}
	
	protected void init(Class<T> elementType, 
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType, 
			DirectMemoryService directMemoryService) {
		this.allocateNonPrimitiveFieldsAtOffHeapConfigType = allocateNonPrimitiveFieldsAtOffHeapConfigType;
		if (allocateNonPrimitiveFieldsAtOffHeapConfigType != null) {
			switch (allocateNonPrimitiveFieldsAtOffHeapConfigType) {
				case NONE:
					break;
				case ONLY_CONFIGURED_NON_PRIMITIVE_FIELDS:
					findNonPrimitiveFieldInitializersForOnlyConfiguredNonPrimitiveFields();
					break;
				case ALL_NON_PRIMITIVE_FIELDS:
					findNonPrimitiveFieldInitializersForAllNonPrimitiveFields();
					break;
			}
		}	
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
	             		jvmAwareObjectFieldInjecter = new Address64BitWithCompressedOopsJvmAwareObjectFieldInjecter();
	             		break;
	             	default:    
	                    throw new AssertionError("Unsupported reference size: " + referenceSize);
	        	}
	        	break;    
	        default:
	            throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());
		} 
	}
	
	@Override
	public Class<T> getElementType() {
		return elementType;
	}
	
	public DirectMemoryService getDirectMemoryService() {
		return directMemoryService;
	}
	
	protected void findNonPrimitiveFieldInitializersForAllNonPrimitiveFields() {
		nonPrimitiveFieldInitializers = new ArrayList<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>>();
		List<Field> fields = ReflectionUtil.getAllFields(elementType);
		for (Field f : fields) {
			if (f.getType().isArray()) {
				nonPrimitiveFieldInitializers.add(new ArrayTypedFieldInitializer(f));
			}
			else if (!f.getType().isPrimitive()) {
				nonPrimitiveFieldInitializers.add(new ComplexTypedFieldInitializer(f));
			}		
		}
	}
	
	protected void findNonPrimitiveFieldInitializersForOnlyConfiguredNonPrimitiveFields() {
		nonPrimitiveFieldInitializers = new ArrayList<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>>();
		OffHeapClassConfig classConfig = offHeapConfigService.getOffHeapClassConfig(getElementType());
		if (classConfig != null) {
			List<OffHeapObjectFieldConfig> objectFieldConfigs = classConfig.getObjectFieldConfigs();
			if (objectFieldConfigs != null) {
				for (OffHeapObjectFieldConfig objectFieldConfig : objectFieldConfigs) {
					nonPrimitiveFieldInitializers.add(new ComplexTypedFieldInitializer(objectFieldConfig));
					if (logger.isInfoEnabled()) {
						logger.info(
							"Created \"ComplexTypedFieldInitializer\" for field " + 
							objectFieldConfig.getField().getName() + 
							" in class " + getElementType().getName());
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
							" in class " + getElementType().getName());
					}
				}
			}
		}
	}
	
	protected T processObject(T obj) {
		if (nonPrimitiveFieldInitializers != null) {
			for (NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig> fieldInitializer : nonPrimitiveFieldInitializers) {
				fieldInitializer.initializeField(obj);
			}
		}
		return obj;
	}
	
	protected long processObject(long objAddress) {
		if (nonPrimitiveFieldInitializers != null) {
			for (NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig> fieldInitializer : nonPrimitiveFieldInitializers) {
				fieldInitializer.initializeField( objAddress);
			}
		}
		return objAddress;
	}
	
	@SuppressWarnings("restriction")
	protected abstract class NonPrimitiveFieldInitializer<C extends OffHeapFieldConfig> {
		
		protected final sun.misc.Unsafe unsafe = JvmUtil.getUnsafe();
		protected C fieldConfig;
		protected final Field field;
		protected final long fieldOffset;
		
		protected NonPrimitiveFieldInitializer(Field field) {
			this.field = field;
			this.fieldOffset = unsafe.objectFieldOffset(field);
		}
		
		protected NonPrimitiveFieldInitializer(C fieldConfig) {
			this.fieldConfig = fieldConfig;
			this.field = fieldConfig.getField();
			this.fieldOffset = unsafe.objectFieldOffset(field);
		}
		
		abstract protected void initializeField(T obj);
		abstract protected void initializeField(long objAddress);
		
	}
	
	protected class ComplexTypedFieldInitializer extends NonPrimitiveFieldInitializer<OffHeapObjectFieldConfig> {
		
		protected Class<?> fieldType;
		
		protected ComplexTypedFieldInitializer(Field field) {
			super(field);
			this.fieldType = field.getType();
		}
		
		protected ComplexTypedFieldInitializer(OffHeapObjectFieldConfig fieldConfig) {
			super(fieldConfig);
			this.fieldType = 
					(fieldConfig.getFieldType() == null || fieldConfig.getFieldType().equals(Object.class)) ? 
							fieldConfig.getField().getType() : 
							fieldConfig.getFieldType();
		}
		
		protected void initializeField(T obj) {
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
	
	protected class ArrayTypedFieldInitializer extends NonPrimitiveFieldInitializer<OffHeapArrayFieldConfig> {
		
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
		
		protected void initializeField(T obj) {
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
	
	protected interface JvmAwareObjectFieldInjecter {
		
		void injectField(long fieldAddress, long fieldObjectAddress);
	
	}
	
	protected class Address32BitJvmAwareObjectFieldInjecter implements JvmAwareObjectFieldInjecter {

		@Override
		public void injectField(long fieldAddress, long fieldObjectAddress) {
			directMemoryService.putAsIntAddress(fieldAddress, fieldObjectAddress);
		}
		
	}
	
	protected class Address64BitWithoutCompressedOopsJvmAwareObjectFieldInjecter implements JvmAwareObjectFieldInjecter {

		@Override
		public void injectField(long fieldAddress, long fieldObjectAddress) {
			directMemoryService.putLong(fieldAddress, fieldObjectAddress);
		}
		
	}
	
	protected class Address64BitWithCompressedOopsJvmAwareObjectFieldInjecter implements JvmAwareObjectFieldInjecter {

		@Override
		public void injectField(long fieldAddress, long fieldObjectAddress) {
			directMemoryService.putAsIntAddress(fieldAddress, fieldObjectAddress);
		}
		
	}
	
}
