/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.pool;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.service.JillegalMemoryService;

@SuppressWarnings("restriction")
public class SequentialObjectPool<T> {

	private Class<T> clazz;
	private long objectCount;
	private long objectSize;
	private long currentMemoryIndex;
	private long allocatedAddress;
	private T sampleObject;
	private long sampleObjectAddress;
	private long addressLimit;
	private Unsafe unsafe;
	
	@SuppressWarnings("unchecked")
	public SequentialObjectPool(Class<T> clazz, long objectCount) {
		this.clazz = clazz;
		this.objectCount = objectCount;
		this.objectSize = JillegalMemoryService.sizeOfWithReflection(clazz);
		this.allocatedAddress = JillegalMemoryService.allocateMemory(objectSize * objectCount);
		this.currentMemoryIndex	= allocatedAddress - objectSize;
		this.addressLimit = allocatedAddress + (objectCount * objectSize) - objectSize;
		this.unsafe	= JillegalMemoryService.getUnsafe();
	
		try {
			this.sampleObject = (T) JillegalMemoryService.getUnsafe().allocateInstance(clazz);
			this.sampleObjectAddress = JillegalMemoryService.addressOf(sampleObject);
			for (long l = 0; l < objectCount; l++) {
				unsafe.copyMemory(sampleObjectAddress, allocatedAddress + (l * objectSize), objectSize);
			}
		} 
		catch (InstantiationException e) {
			e.printStackTrace();
		}
	}
	
	public Class<T> getClazz() {
		return clazz;
	}
	
	public long getObjectCount() {
		return objectCount;
	}
	
	public T newObject() {
		if (currentMemoryIndex >= addressLimit) {
			return null;
		}
		return JillegalMemoryService.getObject((currentMemoryIndex += objectSize));
	}
	
	public T getObject(long objectIndex) {
		if (objectIndex < 0 || objectIndex > objectCount) {
			return null;
		}	
		return JillegalMemoryService.getObject((allocatedAddress + (objectIndex * objectSize)));
	}
	
	public void reset() {
		currentMemoryIndex = allocatedAddress;
	}
	
}
