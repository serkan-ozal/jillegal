/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import tr.com.serkanozal.jcommon.util.ReflectionUtil;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.AllocateAtOffHeap;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
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
	protected List<NonPrimitiveFieldInitializer> nonPrimitiveFieldInitializers;
	protected JvmAwareClassPointerUpdater jvmAwareClassPointerUpdater;
	
	public BaseObjectOffHeapPool(Class<T> elementType) {
		super(elementType);
	}
	
	public BaseObjectOffHeapPool(Class<T> elementType, DirectMemoryService directMemoryService) {
		super(elementType, directMemoryService);
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
		nonPrimitiveFieldInitializers = new ArrayList<NonPrimitiveFieldInitializer>();
		List<Field> fields = ReflectionUtil.getAllFields(elementType);
		for (Field f : fields) {
			if (!f.getType().isPrimitive()) {
				nonPrimitiveFieldInitializers.add(new NonPrimitiveFieldInitializer(f));
			}	
		}
	}
	
	protected void findNonPrimitiveFieldInitializersForOnlyConfiguredNonPrimitiveFields() {
		nonPrimitiveFieldInitializers = new ArrayList<NonPrimitiveFieldInitializer>();
		List<Field> fields = ReflectionUtil.getAllFields(elementType, AllocateAtOffHeap.class);
		for (Field f : fields) {
			if (!f.getType().isPrimitive()) {
				nonPrimitiveFieldInitializers.add(new NonPrimitiveFieldInitializer(f));
			}	
		}
	}
	
	protected T processObject(T obj) {
		if (nonPrimitiveFieldInitializers != null) {
			for (NonPrimitiveFieldInitializer fieldInitializer : nonPrimitiveFieldInitializers) {
				fieldInitializer.initializeField(obj);
			}
		}
		return obj;
	}
	
	@SuppressWarnings("restriction")
	protected class NonPrimitiveFieldInitializer {
		
		protected final sun.misc.Unsafe unsafe = JvmUtil.getUnsafe();
		protected final Field field;
		protected final long fieldOffset;
		
		protected NonPrimitiveFieldInitializer(Field field) {
			this.field = field;
			this.fieldOffset = unsafe.objectFieldOffset(field);
		}
		
		protected void initializeField(T obj) {
			
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
