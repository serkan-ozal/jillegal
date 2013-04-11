/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.SequentialObjectPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;

public class LazyReferencedObjectPool<T> extends BaseOffHeapPool<T, SequentialObjectPoolCreateParameter<T>> {

	private long objectCount;
	private long objectSize;
	private long currentMemoryIndex;
	private long allocatedAddress;
	private T sampleObject;
	private long sampleObjectAddress;
	private long addressLimit;
	
	public LazyReferencedObjectPool(SequentialObjectPoolCreateParameter<T> parameter) {
		this(parameter.getElementType(), parameter.getObjectCount(), parameter.getDirectMemoryService());
	}
	
	public LazyReferencedObjectPool(Class<T> clazz, long objectCount, DirectMemoryService directMemoryService) {
		super(clazz, directMemoryService);
		init(clazz, objectCount, directMemoryService);
	}
	
	protected synchronized void init() {
		this.currentMemoryIndex	= allocatedAddress - objectSize;
		// Copy sample object to allocated memory region for each object
		for (long l = 0; l < objectCount; l++) {
			directMemoryService.copyMemory(sampleObjectAddress, allocatedAddress + (l * objectSize), objectSize);
		}
	}
	
	public long getObjectCount() {
		return objectCount;
	}
	
	@Override
	public synchronized T newObject() {
		if (currentMemoryIndex >= addressLimit) {
			return null;
		}
		return directMemoryService.getObject((currentMemoryIndex += objectSize));
	}
	
	@Override
	public synchronized T getObject(long objectIndex) {
		if (objectIndex < 0 || objectIndex > objectCount) {
			return null;
		}	
		return directMemoryService.getObject((allocatedAddress + (objectIndex * objectSize)));
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
	public void init(SequentialObjectPoolCreateParameter<T> parameter) {
		init(parameter.getElementType(), parameter.getObjectCount(), parameter.getDirectMemoryService());
	}
	
	@SuppressWarnings("unchecked")
	protected void init(Class<T> clazz, long objectCount, DirectMemoryService directMemoryService) {
		this.elementType = clazz;
		this.objectCount = objectCount;
		this.directMemoryService = directMemoryService;
		this.objectSize = directMemoryService.sizeOf(clazz);
		this.allocatedAddress = directMemoryService.allocateMemory(objectSize * objectCount);
		this.addressLimit = allocatedAddress + (objectCount * objectSize) - objectSize;
		this.sampleObject = (T) directMemoryService.allocateInstance(clazz);
		this.sampleObjectAddress = directMemoryService.addressOf(sampleObject);
		
		init();
	}
	
}
