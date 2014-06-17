/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import java.lang.reflect.Array;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.LimitedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.RandomlyReadableOffHeapPool;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public class EagerReferencedObjectOffHeapPool<T> extends BaseObjectOffHeapPool<T, ObjectOffHeapPoolCreateParameter<T>> 
		implements 	ObjectOffHeapPool<T, ObjectOffHeapPoolCreateParameter<T>>,
					LimitedObjectOffHeapPool<T, ObjectOffHeapPoolCreateParameter<T>>,
					RandomlyReadableOffHeapPool<T, ObjectOffHeapPoolCreateParameter<T>>,
					DeeplyForkableObjectOffHeapPool<T, ObjectOffHeapPoolCreateParameter<T>> {

	protected long arraySize;
	protected int currentIndex;
	protected T[] sampleArray;
	protected T[] objectArray;
	protected long objStartAddress;
	
	public EagerReferencedObjectOffHeapPool(ObjectOffHeapPoolCreateParameter<T> parameter) {
		this(parameter.getElementType(), parameter.getObjectCount(), 
				parameter.getAllocateNonPrimitiveFieldsAtOffHeapConfigType(), 
				parameter.getDirectMemoryService());
	}
	
	public EagerReferencedObjectOffHeapPool(Class<T> elementType, int objectCount, 
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType, 
			DirectMemoryService directMemoryService) {
		super(elementType, directMemoryService);
		if (objectCount <= 0) {
			throw new IllegalArgumentException("\"objectCount\" must be positive !");
		}
		init(elementType, objectCount, allocateNonPrimitiveFieldsAtOffHeapConfigType, directMemoryService);
	}
	
	@SuppressWarnings("unchecked")
	protected synchronized void init() {
		this.currentIndex = 0;
		
		int arrayHeaderSize = JvmUtil.getArrayHeaderSize();
		int arrayIndexScale = JvmUtil.arrayIndexScale(elementType);
		long arrayIndexStartAddress = allocatedAddress + JvmUtil.arrayBaseOffset(elementType);
		objStartAddress = allocatedAddress + JvmUtil.sizeOfArray(elementType, objectCount);
		
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
		JvmUtil.setArrayLength(allocatedAddress, elementType, objectCount);
		
		// All index is object pool array header point to allocated objects 
		for (long l = 0; l < objectCount; l++) {
			directMemoryService.putLong(arrayIndexStartAddress + (l * arrayIndexScale), 
					JvmUtil.toJvmAddress((objStartAddress + (l * objectSize))));
		}

		// Copy sample object to allocated memory region for each object
		for (long l = 0; l < objectCount; l++) {
			directMemoryService.copyMemory(offHeapSampleObjectAddress, objStartAddress + (l * objectSize), objectSize);
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
		long address = objStartAddress + (currentIndex * objectSize);
		// Address of class could be changed by GC at "Compact" phase.
		//updateClassPointerOfObject(address);
		return processObject(objectArray[currentIndex++], address);
	}
	
	@Override
	public synchronized long getAsAddress() {
		// TODO Implement
		return 0;
	}
	
	@Override
	public T getAt(int index) {
		if (index < 0 || index >= objectCount) {
			throw new IllegalArgumentException("Invalid index: " + index);
		}	
		long address = objStartAddress + (index * objectSize);
		// Address of class could be changed by GC at "Compact" phase.
		//updateClassPointerOfObject(address);
		return processObject(objectArray[index], address);
	}
	
	public T[] getObjectArray() {
		return objectArray;
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
	public void init(ObjectOffHeapPoolCreateParameter<T> parameter) {
		init(parameter.getElementType(), parameter.getObjectCount(), 
				parameter.getAllocateNonPrimitiveFieldsAtOffHeapConfigType(), 
				parameter.getDirectMemoryService());
	}
	
	@SuppressWarnings("unchecked")
	protected void init(Class<T> elementType, int objectCount, 
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType, 
			DirectMemoryService directMemoryService) {
		super.init(elementType, objectCount, allocateNonPrimitiveFieldsAtOffHeapConfigType, directMemoryService);
		this.arraySize = JvmUtil.sizeOfArray(elementType, objectCount);
		this.allocatedAddress = 
				directMemoryService.allocateMemory(arraySize + (objectCount * objectSize) + 
						JvmUtil.getAddressSize()); // Extra memory for possible aligning
		this.sampleArray = (T[]) Array.newInstance(elementType, 0);
		init();
	}
	
	@Override
	public DeeplyForkableObjectOffHeapPool<T, ObjectOffHeapPoolCreateParameter<T>> fork() {
		return 
			new EagerReferencedObjectOffHeapPool<T>(
						getElementType(), 
						(int)getElementCount(), 
						allocateNonPrimitiveFieldsAtOffHeapConfigType,
						getDirectMemoryService());
	}
	
}
