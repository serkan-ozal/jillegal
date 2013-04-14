/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import java.lang.reflect.Array;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.SequentialObjectOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.LimitedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.RandomlyReadableOffHeapPool;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public class EagerReferencedObjectOffHeapPool<T> extends BaseOffHeapPool<T, SequentialObjectOffHeapPoolCreateParameter<T>> 
		implements 	ObjectOffHeapPool<T, SequentialObjectOffHeapPoolCreateParameter<T>>,
					LimitedObjectOffHeapPool<T, SequentialObjectOffHeapPoolCreateParameter<T>>,
					RandomlyReadableOffHeapPool<T, SequentialObjectOffHeapPoolCreateParameter<T>>,
					DeeplyForkableObjectOffHeapPool<T, SequentialObjectOffHeapPoolCreateParameter<T>> {

	protected int objectCount;
	protected long objectSize;
	protected long arraySize;
	protected int currentIndex;
	protected long allocatedAddress;
	protected T sampleObject;
	protected T[] sampleArray;
	protected T[] objectArray;
	protected long sampleObjectAddress;
	
	public EagerReferencedObjectOffHeapPool(SequentialObjectOffHeapPoolCreateParameter<T> parameter) {
		this(parameter.getElementType(), parameter.getObjectCount(), parameter.getDirectMemoryService());
	}
	
	public EagerReferencedObjectOffHeapPool(Class<T> elementType, int objectCount, DirectMemoryService directMemoryService) {
		super(elementType, directMemoryService);
		if (objectCount <= 0) {
			throw new IllegalArgumentException("\"objectCount\" must be positive !");
		}
		init(elementType, objectCount, directMemoryService);
	}
	
	@SuppressWarnings("unchecked")
	protected synchronized void init() {
		this.currentIndex = 0;
		
		int arrayHeaderSize = JvmUtil.getArrayHeaderSize();
		int arrayIndexScale = JvmUtil.arrayIndexScale(elementType);
		long arrayIndexStartAddress = allocatedAddress + JvmUtil.arrayBaseOffset(elementType);
		long objStartAddress = allocatedAddress + JvmUtil.sizeOfArray(elementType, objectCount);
		
		// Allocated objects must start aligned as address size from start address of allocated address
		long diffBetweenArrayAndObjectStartAddresses = objStartAddress - allocatedAddress;
		long addressMod = diffBetweenArrayAndObjectStartAddresses % JvmUtil.getAddressSize();
		if (addressMod != 0) {
			objStartAddress += (JvmUtil.getAddressSize() - addressMod);
		}

		// Copy sample array header to object pool array header
		for (int i = 0; i < arrayHeaderSize; i++) {
			directMemoryService.putByte(allocatedAddress + i, directMemoryService.getByte(sampleArray, i));
		}
		
		// Set length of array object pool array
		directMemoryService.putInt(arrayIndexStartAddress - JvmUtil.arrayLengthSize(), (int)objectCount);

		// All index is object pool array header point to allocated objects 
		for (long l = 0; l < objectCount; l++) {
			directMemoryService.putLong(arrayIndexStartAddress + (l * arrayIndexScale), 
					JvmUtil.toJvmAddress((objStartAddress + (l * objectSize))));
		}

		// Copy sample object to allocated memory region for each object
		for (long l = 0; l < objectCount; l++) {
			directMemoryService.copyMemory(sampleObjectAddress, objStartAddress + (l * objectSize), objectSize);
		}
		
		this.objectArray = (T[]) directMemoryService.getObject(allocatedAddress);
	}
	
	public int getObjectCount() {
		return objectCount;
	}
	
	@Override
	public synchronized T get() {
		if (currentIndex >= objectCount) {
			return null;
		}
		return objectArray[currentIndex++];
	}
	
	@Override
	public T getAt(int index) {
		if (index < 0 || index >= objectCount) {
			throw new IllegalArgumentException("Invalid index: " + index);
		}	
		return objectArray[index];
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
		this.arraySize = JvmUtil.sizeOfArray(elementType, objectCount);
		this.allocatedAddress = 
				directMemoryService.allocateMemory(arraySize + (objectCount * objectSize) + 
						JvmUtil.getAddressSize()); // Extra memory for possible aligning
		try {
			this.sampleObject = (T) elementType.newInstance();
		} 
		catch (Exception e) {
			e.printStackTrace();
			this.sampleObject = (T) directMemoryService.allocateInstance(elementType);
		} 
		this.sampleArray = (T[]) Array.newInstance(elementType, 0);
		this.sampleObjectAddress = directMemoryService.addressOf(sampleObject);
		
		init();
	}
	
	@Override
	public DeeplyForkableObjectOffHeapPool<T, SequentialObjectOffHeapPoolCreateParameter<T>> fork() {
		return new EagerReferencedObjectOffHeapPool<T>(getElementType(), (int)getElementCount(), getDirectMemoryService());
	}
	
	
}
