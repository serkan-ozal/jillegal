/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectOffHeapPoolCreateParameter;
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
	
	public LazyReferencedObjectOffHeapPool(Class<T> elementType, int objectCount, 
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
		
		this.currentAddress = allocationStartAddress - objectSize;

		long sourceAddress = offHeapSampleObjectAddress + 4;
		long copySize = objectSize - 4;
		// Copy sample object to allocated memory region for each object
		for (long l = 0; l < objectCount; l++) {
			long targetAddress = allocationStartAddress + (l * objectSize);
			directMemoryService.putInt(targetAddress, 0);
			directMemoryService.copyMemory(sourceAddress, targetAddress + 4, copySize);
		}
	}
	
	public long getObjectCount() {
		return objectCount;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized T get() {
		checkAvailability();
		if (currentAddress >= allocationEndAddress) {
			return null;
		}
		long address = (currentAddress += objectSize);
		// Address of class could be changed by GC at "Compact" phase.
		//return directMemoryService.getObject(updateClassPointerOfObject(address));
		return processObject((T) directMemoryService.getObject(address));
	}
	
	@Override
	public synchronized long getAsAddress() {
		checkAvailability();
		if (currentAddress >= allocationEndAddress) {
			return 0;
		}
		long address = (currentAddress += objectSize);
		// Address of class could be changed by GC at "Compact" phase.
		//return directMemoryService.getObject(updateClassPointerOfObject(address));
		return processObject(address);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T getAt(int index) {
		checkAvailability();
		if (index < 0 || index >= objectCount) {
			throw new IllegalArgumentException("Invalid index: " + index);
		}
		long address = allocationStartAddress + (index * objectSize);
		// Address of class could be changed by GC at "Compact" phase.
		//return directMemoryService.getObject(updateClassPointerOfObject(allocatedAddress));
		return processObject((T) directMemoryService.getObject(address));
	}
	
	@Override
	public long getElementCount() {
		return objectCount;
	}

	@Override
	public boolean hasMoreElement() {
		checkAvailability();
		return currentAddress < objectCount;
	}
	
	@Override
	public synchronized void reset() {
		init();
		makeAvaiable();
	}
	
	@Override
	public boolean free(T obj) {
		checkAvailability();
		return false;
	}
	
	@Override
	public boolean freeFromAddress(long objAddress) {
		checkAvailability();
		return false;
	}
	
	@Override
	public synchronized void free() {
		checkAvailability();
		directMemoryService.freeMemory(allocationStartAddress);
		makeUnavaiable();
	}

	@Override
	public synchronized void init(ObjectOffHeapPoolCreateParameter<T> parameter) {
		init(parameter.getElementType(), parameter.getObjectCount(), 
				parameter.getAllocateNonPrimitiveFieldsAtOffHeapConfigType(), 
				parameter.getDirectMemoryService());
	}
	
	protected void init(Class<T> elementType, int objectCount, 
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
						(int)getElementCount(), 
						allocateNonPrimitiveFieldsAtOffHeapConfigType, 
						getDirectMemoryService());
	}
	
}
