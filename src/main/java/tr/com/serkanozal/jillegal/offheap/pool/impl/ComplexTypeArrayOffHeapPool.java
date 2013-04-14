/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import java.lang.reflect.Array;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ArrayOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.pool.ArrayOffHeapPool;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public class ComplexTypeArrayOffHeapPool<T> extends BaseOffHeapPool<T, ArrayOffHeapPoolCreateParameter<T>> 
		implements ArrayOffHeapPool<T, ArrayOffHeapPoolCreateParameter<T>> {

	protected int length;
	protected boolean initializeElements;
	protected long objectSize;
	protected long arraySize;
	protected long allocatedAddress;
	protected T sampleObject;
	protected T[] sampleArray;
	protected T[] objectArray;
	protected long sampleObjectAddress;
	protected long objStartAddress;
	protected long arrayIndexStartAddress;
	protected int arrayIndexScale;
	
	public ComplexTypeArrayOffHeapPool(ArrayOffHeapPoolCreateParameter<T> parameter) {
		this(parameter.getElementType(), parameter.getLength(), parameter.isInitializeElements(), 
				parameter.getDirectMemoryService());
	}
	
	public ComplexTypeArrayOffHeapPool(Class<T> elementType, int length, DirectMemoryService directMemoryService) {
		this(elementType, length, false, directMemoryService);
	}
	
	public ComplexTypeArrayOffHeapPool(Class<T> elementType, int length, boolean initializeElements, DirectMemoryService directMemoryService) {
		super(elementType, directMemoryService);
		if (length <= 0) {
			throw new IllegalArgumentException("\"length\" must be positive !");
		}
		init(elementType, length, initializeElements, directMemoryService);
	}
	
	@SuppressWarnings("unchecked")
	protected synchronized void init() {
		int arrayHeaderSize = JvmUtil.getArrayHeaderSize();
		arrayIndexScale = JvmUtil.arrayIndexScale(elementType);
		arrayIndexStartAddress = allocatedAddress + JvmUtil.arrayBaseOffset(elementType);
		objStartAddress = allocatedAddress + JvmUtil.sizeOfArray(elementType, length);
		
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
		directMemoryService.putInt(arrayIndexStartAddress - JvmUtil.arrayLengthSize(), (int)length);

		if (initializeElements) {
			// All index is object pool array header point to allocated objects 
			for (long l = 0; l < length; l++) {
				directMemoryService.putLong(arrayIndexStartAddress + (l * arrayIndexScale), 
						JvmUtil.toJvmAddress((objStartAddress + (l * objectSize))));
			}
	
			// Copy sample object to allocated memory region for each object
			for (long l = 0; l < length; l++) {
				directMemoryService.copyMemory(sampleObjectAddress, objStartAddress + (l * objectSize), objectSize);
			}
		}
		else {
			// All index is object pool array header point to allocated objects 
			for (long l = 0; l < length; l++) {
				directMemoryService.putLong(arrayIndexStartAddress + (l * arrayIndexScale), 0);
			}
		}
		
		this.objectArray = (T[]) directMemoryService.getObject(allocatedAddress);
	}
	
	public boolean isInitializeElements() {
		return initializeElements;
	}
	
	@Override
	public int getLength() {
		return length;
	}
	
	@Override
	public T getAt(int index) {
		if (index < 0 || index > length) {
			throw new IllegalArgumentException("Invalid index: " + index);
		}	
		return objectArray[index];
	}
	
	@Override
	public void setAt(T element, int index) {
		if (index >= 0 && index < length) {
			// Make target index points to element 
			directMemoryService.putLong(arrayIndexStartAddress + (index * arrayIndexScale), 
					JvmUtil.toJvmAddress(directMemoryService.addressOf(element)));
		}	
		else {
			throw new IllegalArgumentException("Invalid index: " + index);
		}
	}
	
	@Override
	public T[] getArray() {
		return objectArray;
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
	public void init(ArrayOffHeapPoolCreateParameter<T> parameter) {
		init(parameter.getElementType(), parameter.getLength(), parameter.isInitializeElements(), 
				parameter.getDirectMemoryService());
	}
	
	@SuppressWarnings("unchecked")
	protected void init(Class<T> elementType, int length, boolean initializeElements, DirectMemoryService directMemoryService) {
		this.elementType = elementType;
		this.length = length;
		this.initializeElements = initializeElements;
		this.directMemoryService = directMemoryService;
		this.objectSize = directMemoryService.sizeOf(elementType);
		this.arraySize = JvmUtil.sizeOfArray(elementType, length);
		this.allocatedAddress = 
				directMemoryService.allocateMemory(arraySize + (length * objectSize) + 
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

}
