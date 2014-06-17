/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public abstract class BaseObjectOffHeapPool<T, P extends OffHeapPoolCreateParameter<T>> extends BaseOffHeapPool<T, P> {

	protected int objectCount;
	protected long objectSize;
	protected long currentAddress;
	protected long allocatedAddress;
	protected T sampleObject;
	protected long offHeapSampleObjectAddress;
	protected long classPointerAddress;
	protected long classPointerOffset;
	protected long classPointerSize;
	protected long addressLimit;
	protected NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType;
	protected List<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> nonPrimitiveFieldInitializers;
	protected JvmAwareClassPointerUpdater jvmAwareClassPointerUpdater;
	protected OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	protected OffHeapConfigService offHeapConfigService = ConfigManager.getOffHeapConfigService();
	
	public BaseObjectOffHeapPool(Class<T> objectType) {
		super(objectType);
	}
	
	public BaseObjectOffHeapPool(Class<T> objectType, DirectMemoryService directMemoryService) {
		super(objectType, directMemoryService);
	}
	
	@SuppressWarnings("unchecked")
	protected void init(Class<T> elementType, int objectCount, 
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType, 
			DirectMemoryService directMemoryService) {
		if (elementType.isAnnotation()) {
			throw new IllegalArgumentException("Annotation class " + "(" + elementType.getName() + ")" + 
											   " is not supported !");
		}
		if (elementType.isInterface()) {
			throw new IllegalArgumentException("Interface class " + "(" + elementType.getName() + ")" + 
											   " is not supported !");
		}
		if (elementType.isAnonymousClass()) {
			throw new IllegalArgumentException("Anonymous class " + "(" + elementType.getName() + ")" + 
											   " is not supported !");
		}
		this.elementType = elementType;
		this.objectCount = objectCount;
		this.directMemoryService = directMemoryService;
		this.objectSize = directMemoryService.sizeOf(elementType);
		boolean sampleObjectCreated = false;
		try {
			Constructor<T> defaultConstructor = elementType.getConstructor();
			if (defaultConstructor != null) {
				defaultConstructor.setAccessible(true);
				this.sampleObject = defaultConstructor.newInstance();
			}
		} 
		catch (Throwable t) {
			logger.error("Unable to create a sample object for class " + elementType.getName(), t);
		} 
		if (sampleObjectCreated == false) {
			this.sampleObject = (T) directMemoryService.allocateInstance(elementType);
		}
		long address = directMemoryService.addressOf(sampleObject);
		this.offHeapSampleObjectAddress = directMemoryService.allocateMemory(objectSize);
		directMemoryService.copyMemory(address, offHeapSampleObjectAddress, objectSize);
		this.classPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
		this.classPointerOffset = JvmUtil.getClassDefPointerOffsetInObject();
		this.classPointerSize = JvmUtil.getReferenceSize();
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
	        	jvmAwareClassPointerUpdater = new Address32BitJvmClassPointerUpdater();
	            break;
	        case JvmUtil.SIZE_64_BIT:
	        	int referenceSize = JvmUtil.getReferenceSize();
	        	switch (referenceSize) {
	             	case JvmUtil.ADDRESSING_4_BYTE:   
	             		jvmAwareClassPointerUpdater = new Address64BitWithCompressedOopsJvmClassPointerUpdater();
	             		break;
	             	case JvmUtil.ADDRESSING_8_BYTE:
	             		jvmAwareClassPointerUpdater = new Address64BitWithoutCompressedOopsJvmClassPointerUpdater();
	             		break;
	             	default:    
	                    throw new AssertionError("Unsupported reference size: " + referenceSize);
	        	}
	        	break;    
	        default:
	            throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());
		} 
	}
	
	protected boolean checkAndRefreshIfClassPointerOfObjectUpdated() {
		long currentClassPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
		if (currentClassPointerAddress != classPointerAddress) {
			classPointerAddress = currentClassPointerAddress;
			return true;
		}
		else {
			return false;
		}
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
				}
			}
			List<OffHeapArrayFieldConfig> arrayFieldConfigs = classConfig.getArrayFieldConfigs();
			if (arrayFieldConfigs != null) {
				for (OffHeapArrayFieldConfig arrayFieldConfig : arrayFieldConfigs) {
					nonPrimitiveFieldInitializers.add(new ArrayTypedFieldInitializer(arrayFieldConfig));
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
		
		@SuppressWarnings({ "restriction" })
		protected void initializeField(T obj) {
			unsafe.putObject(obj, fieldOffset, offHeapService.newObject(fieldType));
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
		
		@SuppressWarnings("restriction")
		protected void initializeField(T obj) {
			unsafe.putObject(obj, fieldOffset, offHeapService.newArray(arrayType, length));
		}
		
	}
	
	protected interface JvmAwareClassPointerUpdater {
		
		long updateClassPointerOfObject(long address);
	
	}
	
	protected class Address32BitJvmClassPointerUpdater implements JvmAwareClassPointerUpdater {

		@Override
		public long updateClassPointerOfObject(long address) {
			classPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
			directMemoryService.putAsIntAddress(address + classPointerOffset, classPointerAddress);
			return address;
		}
		
	}
	
	protected class Address64BitWithoutCompressedOopsJvmClassPointerUpdater implements JvmAwareClassPointerUpdater {

		@Override
		public long updateClassPointerOfObject(long address) {
			classPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
			directMemoryService.putAsIntAddress(address + classPointerOffset, classPointerAddress);
			return address;
		}
		
	}
	
	protected class Address64BitWithCompressedOopsJvmClassPointerUpdater implements JvmAwareClassPointerUpdater {

		@Override
		public long updateClassPointerOfObject(long address) {
			classPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
			directMemoryService.putLong(address + classPointerOffset, classPointerAddress);
			return address;
		}
		
	}
	
	/**
	 * Get and copy class pointer to current object's class pointer field. 
	 * Address of class could be changed by GC at "Compact" phase.
	 * 
	 * @param address
	 */
	protected long updateClassPointerOfObject(long address) {
		return jvmAwareClassPointerUpdater.updateClassPointerOfObject(address);
	}
	
}
