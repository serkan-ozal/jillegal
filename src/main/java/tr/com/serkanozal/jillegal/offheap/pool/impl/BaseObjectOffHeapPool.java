/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import java.lang.reflect.Constructor;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.pool.ContentAwareOffHeapPool;

public abstract class BaseObjectOffHeapPool<T, P extends OffHeapPoolCreateParameter<T>> 
		extends BaseOffHeapPool<T, P> implements ContentAwareOffHeapPool<T, P> {

	protected int objectCount;
	protected long objectSize;
	protected long currentAddress;
	protected T sampleObject;
	protected long offHeapSampleObjectAddress;
	/*
	protected long classPointerAddress;
	protected long classPointerOffset;
	protected long classPointerSize;
	protected JvmAwareClassPointerUpdater jvmAwareClassPointerUpdater;
	*/
	
	public BaseObjectOffHeapPool(Class<T> objectType) {
		super(objectType);
	}
	
	public BaseObjectOffHeapPool(Class<T> objectType, DirectMemoryService directMemoryService) {
		super(objectType, directMemoryService);
	}
	
	@Override
	protected void init() {
		super.init();
	}
	
	@SuppressWarnings("unchecked")
	protected void init(Class<T> elementType, int objectCount, 
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType, 
			DirectMemoryService directMemoryService) {
		super.init(elementType, allocateNonPrimitiveFieldsAtOffHeapConfigType, directMemoryService);
		
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
		boolean sampleObjectCreated = false;
		try {
			Constructor<T> defaultConstructor = elementType.getConstructor();
			if (defaultConstructor != null) {
				defaultConstructor.setAccessible(true);
				this.sampleObject = defaultConstructor.newInstance();
				sampleObjectCreated = true;
			}
		} 
		catch (Throwable t) {
			//logger.error("Unable to create a sample object for class " + elementType.getName(), t);
		} 
		if (sampleObjectCreated == false) {
			try {
				this.sampleObject = elementType.newInstance();
				sampleObjectCreated = true;
			}	
			catch (Throwable t) {
				//logger.error("Unable to create a sample object for class " + elementType.getName(), t);
			} 
		}
		if (sampleObjectCreated == false) {
			this.sampleObject = (T) directMemoryService.allocateInstance(elementType);
		}
		if (sampleObject == null) {
			throw new IllegalStateException("Unable to create a sample object for class " + elementType.getName());
		}
		long address = directMemoryService.addressOf(sampleObject);
		this.objectSize = directMemoryService.sizeOfObject(sampleObject);
		this.offHeapSampleObjectAddress = directMemoryService.allocateMemory(objectSize);
		directMemoryService.copyMemory(address, offHeapSampleObjectAddress, objectSize);
		/*
		this.classPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
		this.classPointerOffset = JvmUtil.getClassDefPointerOffsetInObject();
		this.classPointerSize = JvmUtil.getReferenceSize();
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
		*/
	}
	
	@Override
	public boolean isMine(T element) {
		if (element == null) {
			return false;
		}
		else {
			return isMine(directMemoryService.addressOf(element));
		}	
	}
	
	@Override
	public boolean isMine(long address) {
		return isIn(address);
	}
	
	/*
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
	*/
	
	/**
	 * Get and copy class pointer to current object's class pointer field. 
	 * Address of class could be changed by GC at "Compact" phase.
	 * 
	 * @param address
	 */
	/*
	protected long updateClassPointerOfObject(long address) {
		return jvmAwareClassPointerUpdater.updateClassPointerOfObject(address);
	}
	*/
	
}
