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
	@Override
	protected void init() {
		super.init();
		
		this.currentIndex = 0;
		
		int arrayHeaderSize = JvmUtil.getArrayHeaderSize();
		int arrayIndexScale = JvmUtil.arrayIndexScale(elementType);
		long arrayIndexStartAddress = allocationStartAddress + JvmUtil.arrayBaseOffset(elementType);
		
		objectsStartAddress = allocationStartAddress + JvmUtil.sizeOfArray(elementType, objectCount);
		// Allocated objects must start aligned as address size from start address of allocated address
		long diffBetweenArrayAndObjectStartAddresses = objectsStartAddress - allocationStartAddress;
		long addressMod = diffBetweenArrayAndObjectStartAddresses % JvmUtil.getAddressSize();
		if (addressMod != 0) {
			objectsStartAddress += (JvmUtil.getAddressSize() - addressMod);
		}
		objectsEndAddress = objectsStartAddress + (objectCount * objectSize);
		
		// Copy sample array header to object pool array header
		for (int i = 0; i < arrayHeaderSize; i++) {
			directMemoryService.putByte(
					allocationStartAddress + i, 
					directMemoryService.getByte(sampleArray, i));
		}

		// Set length of array object pool array
		JvmUtil.setArrayLength(allocationStartAddress, elementType, objectCount);
		
		// All index is object pool array header point to allocated objects 
		for (long l = 0; l < objectCount; l++) {
			directMemoryService.putLong(
					arrayIndexStartAddress + (l * arrayIndexScale), 
					JvmUtil.toJvmAddress((objectsStartAddress + (l * objectSize))));
		}

		long sourceAddress = offHeapSampleObjectAddress + 4;
		long copySize = objectSize - 4;
		// Copy sample object to allocated memory region for each object
		for (long l = 0; l < objectCount; l++) {
			long targetAddress = objectsStartAddress + (l * objectSize);
			directMemoryService.putInt(targetAddress, 0);
			directMemoryService.copyMemory(sourceAddress, targetAddress + 4, copySize);
		}

		this.objectArray = (T[]) directMemoryService.getObject(allocationStartAddress);
	}
	
	public int getObjectCount() {
		return objectCount;
	}
	
	@Override
	public synchronized T get() {
		checkAvailability();
		if (currentIndex >= objectCount) {
			return null;
		}
		// Address of class could be changed by GC at "Compact" phase.
		//updateClassPointerOfObject(address);
		return processObject(objectArray[currentIndex++]);
	}
	
	@Override
	public synchronized long getAsAddress() {
		checkAvailability();
		if (currentIndex >= objectCount) {
			return 0;
		}
		long address = objectsStartAddress + (currentIndex++ * objectSize);
		// Address of class could be changed by GC at "Compact" phase.
		//updateClassPointerOfObject(address);
		return processObject(address);
	}
	
	@Override
	public T getAt(int index) {
		checkAvailability();
		if (index < 0 || index >= objectCount) {
			throw new IllegalArgumentException("Invalid index: " + index);
		}	
		// Address of class could be changed by GC at "Compact" phase.
		//updateClassPointerOfObject(address);
		return processObject(objectArray[index]);
	}
	
	public T[] getObjectArray() {
		checkAvailability();
		return objectArray;
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
	public boolean free(T obj) {
		checkAvailability();
		return freeFromAddress(directMemoryService.addressOf(obj));
	}
	
	@Override
	public boolean freeFromAddress(long objAddress) {
		checkAvailability();
		setUnsetInUseBit(objAddress, false);
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
	
	@SuppressWarnings("unchecked")
	protected void init(Class<T> elementType, int objectCount, 
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType, 
			DirectMemoryService directMemoryService) {
		super.init(elementType, objectCount, allocateNonPrimitiveFieldsAtOffHeapConfigType, directMemoryService);
		this.arraySize = JvmUtil.sizeOfArray(elementType, objectCount);
		this.allocationSize = 
				arraySize + (objectCount * objectSize) + JvmUtil.getAddressSize(); // Extra memory for possible aligning;
		this.allocationStartAddress = directMemoryService.allocateMemory(allocationSize); 
		this.allocationEndAddress = allocationStartAddress + (objectCount * objectSize) - objectSize;;
		this.sampleArray = (T[]) Array.newInstance(elementType, 0);
		init();
		
		makeAvaiable();
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
