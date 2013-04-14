/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.SequentialObjectOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.LimitedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.RandomlyReadableOffHeapPool;

public class LazyReferencedObjectOffHeapPool<T> extends BaseOffHeapPool<T, SequentialObjectOffHeapPoolCreateParameter<T>> 
		implements 	ObjectOffHeapPool<T, SequentialObjectOffHeapPoolCreateParameter<T>>, 
					LimitedObjectOffHeapPool<T, SequentialObjectOffHeapPoolCreateParameter<T>>,
					RandomlyReadableOffHeapPool<T, SequentialObjectOffHeapPoolCreateParameter<T>>,
					DeeplyForkableObjectOffHeapPool<T, SequentialObjectOffHeapPoolCreateParameter<T>> {

	protected int objectCount;
	protected long objectSize;
	protected long currentIndex;
	protected long allocatedAddress;
	protected T sampleObject;
	protected long sampleObjectAddress;
	protected long addressLimit;
	
	public LazyReferencedObjectOffHeapPool(SequentialObjectOffHeapPoolCreateParameter<T> parameter) {
		this(parameter.getElementType(), parameter.getObjectCount(), parameter.getDirectMemoryService());
	}
	
	public LazyReferencedObjectOffHeapPool(Class<T> elementType, int objectCount, DirectMemoryService directMemoryService) {
		super(elementType, directMemoryService);
		if (objectCount <= 0) {
			throw new IllegalArgumentException("\"objectCount\" must be positive !");
		}
		init(elementType, objectCount, directMemoryService);
	}
	
	protected synchronized void init() {
		this.currentIndex = allocatedAddress - objectSize;
		
		// Copy sample object to allocated memory region for each object
		for (long l = 0; l < objectCount; l++) {
			directMemoryService.copyMemory(sampleObjectAddress, allocatedAddress + (l * objectSize), objectSize);
		}
	}
	
	public long getObjectCount() {
		return objectCount;
	}
	
	@Override
	public synchronized T get() {
		if (currentIndex >= addressLimit) {
			return null;
		}
		return directMemoryService.getObject((currentIndex += objectSize));
	}
	
	@Override
	public T getAt(int index) {
		if (index < 0 || index >= objectCount) {
			throw new IllegalArgumentException("Invalid index: " + index);
		}	
		return directMemoryService.getObject((allocatedAddress + (index * objectSize)));
	}
	
	@Override
	public long getElementCount() {
		return objectCount;
	}

	@Override
	public boolean hasMoreElement() {
		return currentIndex < objectCount;
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
	public void init(SequentialObjectOffHeapPoolCreateParameter<T> parameter) {
		init(parameter.getElementType(), parameter.getObjectCount(), parameter.getDirectMemoryService());
	}
	
	@SuppressWarnings("unchecked")
	protected void init(Class<T> elementType, int objectCount, DirectMemoryService directMemoryService) {
		this.elementType = elementType;
		this.objectCount = objectCount;
		this.directMemoryService = directMemoryService;
		this.objectSize = directMemoryService.sizeOf(elementType);
		this.allocatedAddress = directMemoryService.allocateMemory(objectSize * objectCount);
		this.addressLimit = allocatedAddress + (objectCount * objectSize) - objectSize;
		this.sampleObject = (T) directMemoryService.allocateInstance(elementType);
		this.sampleObjectAddress = directMemoryService.addressOf(sampleObject);
		
		init();
	}

	@Override
	public DeeplyForkableObjectOffHeapPool<T, SequentialObjectOffHeapPoolCreateParameter<T>> fork() {
		return new LazyReferencedObjectOffHeapPool<T>(getElementType(), (int)getElementCount(), getDirectMemoryService());
	}
	
}
