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
import tr.com.serkanozal.jillegal.offheap.exception.ObjectInUseException;
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
	protected long arrayStartAddress;
	protected T[] sampleArray;
	protected T[] objectArray;
	
	public EagerReferencedObjectOffHeapPool(ObjectOffHeapPoolCreateParameter<T> parameter) {
		this(parameter.getElementType(), parameter.getObjectCount(), 
				parameter.getAllocateNonPrimitiveFieldsAtOffHeapConfigType(), 
				parameter.getDirectMemoryService());
	}
	
	public EagerReferencedObjectOffHeapPool(Class<T> elementType, long objectCount, 
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
		
		int arrayIndexScale = JvmUtil.arrayIndexScale(elementType);
		long arrayIndexStartAddress = arrayStartAddress + JvmUtil.arrayBaseOffset(elementType);

		// All index in object pool array header point to allocated objects 
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

		this.objectArray = (T[]) directMemoryService.getObject(arrayStartAddress);
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
		//updateClassPointerOfObject(currentAddress);
		return takeObject(objectArray[(int) currentIndex]);
	}
	
	@Override
	public synchronized long getAsAddress() {
		checkAvailability();
		if (!nextAvailable()) {
			return JvmUtil.NULL;
		}
		long address = objectsStartAddress + (currentIndex * objectSize);
		// Address of class could be changed by GC at "Compact" phase.
		//updateClassPointerOfObject(address);
		return takeObjectAsAddress(address);
	}
	
	@Override
	public T getAt(int index) {
		checkAvailability();
		if (index < 0 || index >= objectCount) {
			throw new IllegalArgumentException("Invalid index: " + index);
		}	
		if (getInUseFromObjectIndex(index) != OBJECT_IS_AVAILABLE) {
			throw new ObjectInUseException(index);
		}
		// Address of class could be changed by GC at "Compact" phase.
		//long address = objectsStartAddress + (index * objectSize);
		//updateClassPointerOfObject(address);
		return takeObject(objectArray[index]);
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
	public synchronized boolean free(T obj) {
		checkAvailability();
		if (obj == null) {
			return false;
		}
		return releaseObject(directMemoryService.addressOf(obj));
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
	
	@SuppressWarnings("unchecked")
	protected void init(Class<T> elementType, long objectCount, 
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType, 
			DirectMemoryService directMemoryService) {
		if (objectCount > Integer.MAX_VALUE) {
			throw 
				new IllegalArgumentException(
						"Maximum " + Integer.MAX_VALUE + " object are alloved for " + 
						"EagerReferencedObjectOffHeapPool");
		}
		try {
			super.init(elementType, objectCount, allocateNonPrimitiveFieldsAtOffHeapConfigType, directMemoryService);
			arraySize = JvmUtil.sizeOfArray(elementType, objectCount);
			allocationSize = 
					arraySize + (objectCount * objectSize) + 
					2 * JvmUtil.OBJECT_ADDRESS_SENSIVITY; // Extra memory for possible aligning;
			allocationStartAddress = directMemoryService.allocateMemory(allocationSize); 
			allocationEndAddress = allocationStartAddress + (objectCount * objectSize) - objectSize;;
			sampleArray = (T[]) Array.newInstance(elementType, 0);
			
			long addressMod;
			
			arrayStartAddress = allocationStartAddress;
			addressMod = arrayStartAddress % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
			if (addressMod != 0) {
				arrayStartAddress += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - addressMod);
			}
			
			objectsStartAddress = arrayStartAddress + JvmUtil.sizeOfArray(elementType, objectCount);
			// Allocated objects must start aligned as address size from start address of allocated address
			long diffBetweenArrayAndObjectStartAddresses = objectsStartAddress - arrayStartAddress;
			addressMod = diffBetweenArrayAndObjectStartAddresses % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
			if (addressMod != 0) {
				objectsStartAddress += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - addressMod);
			}
			objectsEndAddress = objectsStartAddress + (objectCount * objectSize);
			
			int arrayHeaderSize = JvmUtil.getArrayHeaderSize();
			// Copy sample array header to object pool array header
			for (int i = 0; i < arrayHeaderSize; i++) {
				directMemoryService.putByte(
						arrayStartAddress + i, 
						directMemoryService.getByte(sampleArray, i));
			}

			// Set length of array object pool array
			JvmUtil.setArrayLength(arrayStartAddress, elementType, (int) objectCount);
			
			init();
			
			makeAvaiable();
		}
		catch (IllegalArgumentException e) {
			throw e;
		}
		catch (Throwable t) {
			logger.error("Error occured while initializing \"EagerReferencedObjectOffHeapPool\"", t);
			throw new IllegalStateException(t);
		}	
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
