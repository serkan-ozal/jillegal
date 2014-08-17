/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.exception.ObjectInUseException;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.LimitedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.RandomlyReadableOffHeapPool;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public class LazyReferencedObjectOffHeapPool<T> extends BaseObjectOffHeapPool<T, ObjectOffHeapPoolCreateParameter<T>> 
		implements 	ObjectOffHeapPool<T, ObjectOffHeapPoolCreateParameter<T>>, 
					LimitedObjectOffHeapPool<T, ObjectOffHeapPoolCreateParameter<T>>,
					RandomlyReadableOffHeapPool<T, ObjectOffHeapPoolCreateParameter<T>>,
					DeeplyForkableObjectOffHeapPool<T, ObjectOffHeapPoolCreateParameter<T>> {

	public LazyReferencedObjectOffHeapPool(ObjectOffHeapPoolCreateParameter<T> parameter) {
		this(parameter.getElementType(), parameter.getObjectCount(), 
				parameter.getAllocateNonPrimitiveFieldsAtOffHeapConfigType(), 
				parameter.getDirectMemoryService());
	}
	
	public LazyReferencedObjectOffHeapPool(Class<T> elementType, long objectCount, 
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType, 
			DirectMemoryService directMemoryService) {
		super(elementType, directMemoryService);
		if (objectCount <= 0) {
			throw new IllegalArgumentException("\"objectCount\" must be positive !");
		}
		init(elementType, objectCount, allocateNonPrimitiveFieldsAtOffHeapConfigType, directMemoryService);
	}
	
	@Override
	protected void init() {
		super.init();
		
		objectsStartAddress = allocationStartAddress;
		// Allocated objects must start aligned as address size from start address of allocated address
		long addressMod = objectsStartAddress % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
		if (addressMod != 0) {
			objectsStartAddress += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - addressMod);
		}
		objectsEndAddress = objectsStartAddress + (objectCount * objectSize);

		long sourceAddress = offHeapSampleObjectAddress + 4;
		long copySize = objectSize - 4;
		// Copy sample object to allocated memory region for each object
		for (long l = 0; l < objectCount; l++) {
			long targetAddress = objectsStartAddress + (l * objectSize);
			directMemoryService.putInt(targetAddress, 0);
			directMemoryService.copyMemory(sourceAddress, targetAddress + 4, copySize);
		}
	}
	
	public long getObjectCount() {
		return objectCount;
	}
	
	@Override
	public synchronized T get() {
		checkAvailability();
		if (!nextAvailable()) {
			return null;
		}
		// Address of class could be changed by GC at "Compact" phase.
		//return directMemoryService.getObject(updateClassPointerOfObject(currentAddress));
		return takeObject(currentAddress);
	}
	
	@Override
	public synchronized long getAsAddress() {
		checkAvailability();
		if (!nextAvailable()) {
			return JvmUtil.NULL;
		}
		// Address of class could be changed by GC at "Compact" phase.
		//return directMemoryService.getObject(updateClassPointerOfObject(currentAddress));
		return takeObjectAsAddress(currentAddress);
	}
	
	@Override
	public T getAt(int index) {
		checkAvailability();
		if (index < 0 || index >= objectCount) {
			throw new IllegalArgumentException("Invalid index: " + index);
		}
		long address = objectsStartAddress + (index * objectSize);
		if (getInUseFromObjectAddress(address) != OBJECT_IS_AVAILABLE) {
			throw new ObjectInUseException(index);
		}
		// Address of class could be changed by GC at "Compact" phase.
		//return directMemoryService.getObject(updateClassPointerOfObject(address));
		return takeObject(address);
	}
	
	@Override
	public long getElementCount() {
		return objectCount;
	}

	@Override
	public boolean hasMoreElement() {
		checkAvailability();
		return currentIndex < objectCount;
	}
	
	@Override
	public synchronized void reset() {
		init();
		makeAvaiable();
	}
	
	@Override
	public synchronized boolean free(T obj) {
		checkAvailability();
		if (obj == null) {
			return false;
		}
		return releaseObject(obj);
	}
	
	@Override
	public synchronized boolean freeFromAddress(long objAddress) {
		checkAvailability();
		return releaseObject(objAddress);
	}

	@Override
	public synchronized void init(ObjectOffHeapPoolCreateParameter<T> parameter) {
		init(parameter.getElementType(), parameter.getObjectCount(), 
				parameter.getAllocateNonPrimitiveFieldsAtOffHeapConfigType(), 
				parameter.getDirectMemoryService());
	}
	
	protected void init(Class<T> elementType, long objectCount, 
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType, 
			DirectMemoryService directMemoryService) {
		super.init(elementType, objectCount, allocateNonPrimitiveFieldsAtOffHeapConfigType, directMemoryService);
		this.allocationSize = 
				objectSize * objectCount + JvmUtil.getAddressSize(); // Extra memory for possible aligning)
		this.allocationStartAddress = directMemoryService.allocateMemory(allocationSize); 
		this.allocationEndAddress = allocationStartAddress + (objectCount * objectSize) - objectSize;
		init();
		makeAvaiable();
	}

	@Override
	public DeeplyForkableObjectOffHeapPool<T, ObjectOffHeapPoolCreateParameter<T>> fork() {
		return 
			new LazyReferencedObjectOffHeapPool<T>(
						getElementType(), 
						getElementCount(), 
						allocateNonPrimitiveFieldsAtOffHeapConfigType, 
						getDirectMemoryService());
	}
	
}
