/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

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
		this(parameter.getElementType(), parameter.getObjectCount(), parameter.getDirectMemoryService());
	}
	
	public LazyReferencedObjectOffHeapPool(Class<T> elementType, int objectCount, 
			DirectMemoryService directMemoryService) {
		super(elementType, directMemoryService);
		if (objectCount <= 0) {
			throw new IllegalArgumentException("\"objectCount\" must be positive !");
		}
		init(elementType, objectCount, directMemoryService);
	}
	
	protected synchronized void init() {
		this.currentAddress = allocatedAddress - objectSize;
		
		// Copy sample object to allocated memory region for each object
		for (long l = 0; l < objectCount; l++) {
			directMemoryService.copyMemory(offHeapSampleObjectAddress, allocatedAddress + (l * objectSize), objectSize);
		}
	}
	
	public long getObjectCount() {
		return objectCount;
	}
	
	@Override
	public synchronized T get() {
		if (currentAddress >= addressLimit) {
			return null;
		}
		// Address of class could be changed by GC at "Compact" phase.
		//return directMemoryService.getObject(updateClassPointerOfObject((currentAddress += objectSize)));
		return directMemoryService.getObject(currentAddress += objectSize);
	}
	
	@Override
	public T getAt(int index) {
		if (index < 0 || index >= objectCount) {
			throw new IllegalArgumentException("Invalid index: " + index);
		}
		// Address of class could be changed by GC at "Compact" phase.
		//return directMemoryService.getObject(updateClassPointerOfObject((allocatedAddress + (index * objectSize))));
		return directMemoryService.getObject(allocatedAddress + (index * objectSize));
	}
	
	@Override
	public long getElementCount() {
		return objectCount;
	}

	@Override
	public boolean hasMoreElement() {
		return currentAddress < objectCount;
	}
	
	@Override
	public void reset() {
		init();
	}
	
	@Override
	public void free() {
		directMemoryService.freeMemory(allocatedAddress);
	}

	@Override
	public void init(ObjectOffHeapPoolCreateParameter<T> parameter) {
		init(parameter.getElementType(), parameter.getObjectCount(), parameter.getDirectMemoryService());
	}
	
	protected void init(Class<T> elementType, int objectCount, DirectMemoryService directMemoryService) {
		super.init(elementType, objectCount, directMemoryService);
		this.allocatedAddress = directMemoryService.allocateMemory(objectSize * objectCount + 
									JvmUtil.getAddressSize()); // Extra memory for possible aligning);
		this.addressLimit = allocatedAddress + (objectCount * objectSize) - objectSize;
		init();
	}

	@Override
	public DeeplyForkableObjectOffHeapPool<T, ObjectOffHeapPoolCreateParameter<T>> fork() {
		return 
			new LazyReferencedObjectOffHeapPool<T>(
						getElementType(), 
						(int)getElementCount(), 
						getDirectMemoryService());
	}
	
}
