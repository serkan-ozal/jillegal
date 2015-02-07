/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import java.lang.reflect.Array;

import tr.com.serkanozal.jillegal.offheap.OffHeapAwareObject;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ArrayOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.pool.ContentAwareOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableArrayOffHeapPool;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public class ComplexTypeArrayOffHeapPool<T, A> extends BaseOffHeapPool<T, ArrayOffHeapPoolCreateParameter<T>> 
		implements 	DeeplyForkableArrayOffHeapPool<T, A, ArrayOffHeapPoolCreateParameter<T>>,
					ContentAwareOffHeapPool<T, ArrayOffHeapPoolCreateParameter<T>> {

	protected int length;
	protected boolean initializeElements;
	protected long objectSize;
	protected long arraySize;
	protected T sampleObject;
	protected A sampleArray;
	// protected A objectArray;
	protected long sampleObjectAddress;
	protected long objStartAddress;
	protected long arrayStartAddress;
	protected long arrayIndexStartAddress;
	protected int arrayIndexScale;
	protected JvmAwareArrayElementAddressFinder jvmAwareArrayElementAddressFinder;
	
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
	@Override
	protected void init() {
		super.init();
		
		if (initializeElements) {
			// All index is object pool array header point to allocated objects 
			for (long l = 0; l < length; l++) {
				directMemoryService.putLong(arrayIndexStartAddress + (l * arrayIndexScale), 
						JvmUtil.toJvmAddress((objStartAddress + (l * objectSize))));
			}
	
			// Copy sample object to allocated memory region for each object
			for (long l = 0; l < length; l++) {
				directMemoryService.copyMemory(
						sampleObjectAddress, 
						objStartAddress + (l * objectSize), 
						objectSize);
			}
			
			/*
			long sourceAddress = sampleObjectAddress + 4;
			long copySize = objectSize - 4;
			// Copy sample object to allocated memory region for each object
			for (long l = 0; l < length; l++) {
				long targetAddress = objStartAddress + (l * objectSize);
				directMemoryService.putInt(targetAddress, 0);
				directMemoryService.copyMemory(sourceAddress, targetAddress + 4, copySize);
			}
			*/
		}
		else {
			// All indexes in object pool array point to empty (null) object
			for (long l = 0; l < length; l++) {
				directMemoryService.putLong(arrayIndexStartAddress + (l * arrayIndexScale), 0L);
			}
		}
		
		// this.objectArray = (A) directMemoryService.getObject(arrayStartAddress);
	}
	
	@SuppressWarnings("unchecked")
	private A getObjectArray() {
		return (A) directMemoryService.getObject(arrayStartAddress);
	}
	
	public boolean isInitializeElements() {
		return initializeElements;
	}
	
	@Override
	public int getLength() {
		return length;
	}
	
	@Override
	public boolean isMine(T element) {
		checkAvailability();
		if (element == null) {
			return false;
		}
		else {
			return isMine(directMemoryService.addressOf(element));
		}	
	}

	@Override
	public boolean isMine(long address) {
		checkAvailability();
		return isIn(address);
	}
	
	@Override
	public boolean isMe(A array) {
		checkAvailability();
		return getObjectArray() == array;
	}
	
	@Override
	public boolean isMeAsAddress(long arrayAddress) {
		checkAvailability();
		return allocationStartAddress == arrayAddress;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T getAt(int index) {
		checkAvailability();
		if (index < 0 || index > length) {
			throw new IllegalArgumentException("Invalid index: " + index);
		}	
		processObject(
				jvmAwareArrayElementAddressFinder.findAddress(
						arrayIndexStartAddress, arrayIndexScale, index));
		T obj = ((T[])(getObjectArray()))[index];
		if (obj instanceof OffHeapAwareObject) {
			((OffHeapAwareObject) obj).onGet(offHeapService, directMemoryService);
		}
		return obj;
	}
	
	@Override
	public void setAt(T element, int index) {
		checkAvailability();
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
	public A getArray() {
		checkAvailability();
		return getObjectArray();
	}
	
	@Override
	public long getArrayAsAddress() {
		checkAvailability();
		return allocationStartAddress;
	}
	
	@Override
	public synchronized void reset() {
		init();
		makeAvaiable();
	}
	
	@Override
	public synchronized void free() {
		checkAvailability();
		// TODO Iterate over all objects and call their "onFree" methods if they are instance of "OffHeapAwareObject"
		directMemoryService.freeMemory(allocationStartAddress);
		directMemoryService.freeMemory(sampleObjectAddress);
		makeUnavaiable();
	}

	@Override
	public void init(ArrayOffHeapPoolCreateParameter<T> parameter) {
		init(parameter.getElementType(), parameter.getLength(), parameter.isInitializeElements(), 
				parameter.getDirectMemoryService());
	}
	
	@SuppressWarnings("unchecked")
	protected void init(Class<T> elementType, int length, boolean initializeElements, DirectMemoryService directMemoryService) {
		try {
			this.elementType = elementType;
			this.length = length;
			this.initializeElements = initializeElements;
			this.directMemoryService = directMemoryService;
			
			objectSize = directMemoryService.sizeOfClass(elementType);
			arraySize = JvmUtil.sizeOfArray(elementType, length);
			if (initializeElements) {
				allocationSize = 
						arraySize + (length * objectSize) + 
						2 * JvmUtil.OBJECT_ADDRESS_SENSIVITY; // Extra memory for possible aligning
			}
			else {
				allocationSize = 
						arraySize + 
						2 * JvmUtil.OBJECT_ADDRESS_SENSIVITY; // Extra memory for possible aligning
			}
			allocationStartAddress = directMemoryService.allocateMemory(allocationSize); 
			allocationEndAddress = allocationStartAddress + allocationSize;

			sampleArray = (A) Array.newInstance(elementType, 0);
			
			if (initializeElements) {
				sampleObject = JvmUtil.getSampleInstance(elementType);
				if (sampleObject == null) {
					throw new IllegalStateException("Unable to create a sample object for class " + elementType.getName());
				}
				sampleObjectAddress = directMemoryService.allocateMemory(objectSize);
				directMemoryService.copyMemory(sampleObject, 0L, null, sampleObjectAddress, objectSize);
			}

			long addressMod;
			
			arrayStartAddress = allocationStartAddress;
			addressMod = arrayStartAddress % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
			if (addressMod != 0) {
				arrayStartAddress += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - addressMod);
			}
			
			int arrayHeaderSize = JvmUtil.getArrayHeaderSize();
			arrayIndexScale = JvmUtil.arrayIndexScale(elementType);
			arrayIndexStartAddress = arrayStartAddress + JvmUtil.arrayBaseOffset(elementType);
			
			if (initializeElements) {
				objStartAddress = arrayStartAddress + JvmUtil.sizeOfArray(elementType, length);
				
				// Allocated objects must start aligned as address size from start address of allocated address
				long diffBetweenArrayAndObjectStartAddresses = objStartAddress - arrayStartAddress;
				addressMod = diffBetweenArrayAndObjectStartAddresses % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
				if (addressMod != 0) {
					objStartAddress += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - addressMod);
				}
			}
			
			// Copy sample array header to object pool array header
			directMemoryService.copyMemory(sampleArray, 0L, null, arrayStartAddress, arrayHeaderSize);

			// Set length of array object pool array
			JvmUtil.setArrayLength(arrayStartAddress, elementType, length);

			switch (JvmUtil.getAddressSize()) {
		        case JvmUtil.SIZE_32_BIT:
		        	jvmAwareArrayElementAddressFinder = new Address32BitJvmAwareArrayElementAddressFinder();
		            break;
		        case JvmUtil.SIZE_64_BIT:
		        	int referenceSize = JvmUtil.getReferenceSize();
		        	switch (referenceSize) {
		             	case JvmUtil.ADDRESSING_4_BYTE:   
		             		jvmAwareArrayElementAddressFinder = new Address64BitWithCompressedOopsJvmAwareArrayElementAddressFinder();
		             		break;
		             	case JvmUtil.ADDRESSING_8_BYTE:
		             		jvmAwareArrayElementAddressFinder = new Address64BitWithCompressedOopsJvmAwareArrayElementAddressFinder();
		             		break;
		             	default:    
		                    throw new AssertionError("Unsupported reference size: " + referenceSize);
		        	}
		        	break;    
		        default:
		            throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());
			} 
			
			init();
			
			makeAvaiable();
		}
		catch (IllegalArgumentException e) {
			throw e;
		}
		catch (Throwable t) {
			logger.error("Error occured while initializing \"ComplexTypeArrayOffHeapPool\"", t);
			throw new IllegalStateException(t);
		}		
	}
	
	@Override
	public DeeplyForkableArrayOffHeapPool<T, A, ArrayOffHeapPoolCreateParameter<T>> fork() {
		return 
				new ComplexTypeArrayOffHeapPool<T, A>(
							getElementType(), 
							length, 
							getDirectMemoryService());
	}
	
	protected interface JvmAwareArrayElementAddressFinder {
		
		long findAddress(long arrayIndexStartAddress, int arrayIndexScale, int index);
	
	}
	
	protected class Address32BitJvmAwareArrayElementAddressFinder implements JvmAwareArrayElementAddressFinder {

		@Override
		public long findAddress(long arrayIndexStartAddress, int arrayIndexScale, int index) {
			return directMemoryService.getAsIntAddress(arrayIndexStartAddress + (arrayIndexScale * index));
		}
		
	}
	
	protected class Address64BitWithoutCompressedOopsJvmAwareArrayElementAddressFinder implements JvmAwareArrayElementAddressFinder {

		@Override
		public long findAddress(long arrayIndexStartAddress, int arrayIndexScale, int index) {
			return directMemoryService.getLong(arrayIndexStartAddress + (arrayIndexScale * index));
		}
		
	}
	
	protected class Address64BitWithCompressedOopsJvmAwareArrayElementAddressFinder implements JvmAwareArrayElementAddressFinder {

		@Override
		public long findAddress(long arrayIndexStartAddress, int arrayIndexScale, int index) {
			return directMemoryService.getAsIntAddress(arrayIndexStartAddress + (arrayIndexScale * index));
		}
		
	}

}
