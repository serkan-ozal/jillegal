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
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableArrayOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ContentAwareOffHeapPool;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public class PrimitiveTypeArrayOffHeapPool<T, A> extends BaseOffHeapPool<T, ArrayOffHeapPoolCreateParameter<T>> 
		implements 	DeeplyForkableArrayOffHeapPool<T, A, ArrayOffHeapPoolCreateParameter<T>>, 
					ContentAwareOffHeapPool<T, ArrayOffHeapPoolCreateParameter<T>> {

	protected int length;
	protected int elementSize;
	protected long arraySize;
	protected long arrayIndexStartAddress;
	protected long arrayStartAddress;
	protected int arrayIndexScale;
	protected A sampleArray;
	protected A primitiveArray;
	
	public PrimitiveTypeArrayOffHeapPool(ArrayOffHeapPoolCreateParameter<T> parameter) {
		this(parameter.getElementType(), parameter.getLength(), parameter.getDirectMemoryService());
	}

	public PrimitiveTypeArrayOffHeapPool(Class<T> elementType, int length,  DirectMemoryService directMemoryService) {
		super(elementType, directMemoryService);
		if (length <= 0) {
			throw new IllegalArgumentException("\"length\" must be positive !");
		}
		init(elementType, length, directMemoryService);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void init() {
		super.init();

		// Clear array content
		directMemoryService.setMemory(arrayIndexStartAddress, arrayIndexScale * length, (byte) 0);
		
		primitiveArray = (A) directMemoryService.getObject(arrayStartAddress);
	}
	
	@Override
	public int getLength() {
		return length;
	}
	
	@Override
	public boolean isMe(A array) {
		checkAvailability();
		return primitiveArray == array;
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
			return null;
		}	
		return ((T[])(primitiveArray))[index];
	}
	
	@Override
	public void setAt(T element, int index) {
		checkAvailability();
		if (index >= 0 && index < length) {
			if (element.getClass().equals(boolean.class) || element.getClass().equals(Boolean.class)) {
				directMemoryService.putByte(arrayIndexStartAddress + (index * arrayIndexScale), 
						(byte)(((Boolean)element) ? 1 : 0));
			}
			else if (element.getClass().equals(byte.class) || element.getClass().equals(Byte.class)) {
				directMemoryService.putInt(arrayIndexStartAddress + (index * arrayIndexScale), (Byte)element);
			}
			else if (element.getClass().equals(char.class) || element.getClass().equals(Character.class)) {
				directMemoryService.putChar(arrayIndexStartAddress + (index * arrayIndexScale), (Character)element);
			}
			else if (element.getClass().equals(short.class) || element.getClass().equals(Short.class)) {
				directMemoryService.putShort(arrayIndexStartAddress + (index * arrayIndexScale), (Short)element);
			}
			else if (element.getClass().equals(int.class) || element.getClass().equals(Integer.class)) {
				directMemoryService.putInt(arrayIndexStartAddress + (index * arrayIndexScale), (Integer)element);
			}
			else if (element.getClass().equals(float.class) || element.getClass().equals(Float.class)) {
				directMemoryService.putFloat(arrayIndexStartAddress + (index * arrayIndexScale), (Float)element);
			}
			else if (element.getClass().equals(long.class) || element.getClass().equals(Long.class)) {
				directMemoryService.putLong(arrayIndexStartAddress + (index * arrayIndexScale), (Long)element);
			}
			else if (element.getClass().equals(double.class) || element.getClass().equals(Double.class)) {
				directMemoryService.putDouble(arrayIndexStartAddress + (index * arrayIndexScale), (Double)element);
			}
			else {
				throw new IllegalArgumentException(getClass().getSimpleName() + " supports only primitive types !");
			}
		}	
		else {
			throw new IllegalArgumentException("Invalid index: " + index);
		}
	}
	
	@Override
	public A getArray() {
		checkAvailability();
		return primitiveArray;
	}
	
	@Override
	public long getArrayAsAddress() {
		checkAvailability();
		return allocationStartAddress;
	}
	
	@Override
	public boolean isMine(T element) {
		checkAvailability();
		return false;
	}

	@Override
	public boolean isMine(long address) {
		checkAvailability();
		return isIn(address);
	}
	
	@Override
	public synchronized void reset() {
		init();
		makeAvaiable();
	}
	
	@Override
	public synchronized void free() {
		checkAvailability();
		directMemoryService.freeMemory(allocationStartAddress);
		makeUnavaiable();
	}

	@Override
	public synchronized void init(ArrayOffHeapPoolCreateParameter<T> parameter) {
		init(parameter.getElementType(), parameter.getLength(), parameter.getDirectMemoryService());
	}
	
	@SuppressWarnings("unchecked")
	protected void init(Class<T> elementType, int length, DirectMemoryService directMemoryService) {
		try {
			this.elementType = elementType;
			this.length = length;
			this.directMemoryService = directMemoryService;
			
			elementSize = JvmUtil.sizeOfType(elementType);
			arraySize = JvmUtil.sizeOfArray(elementType, length);
			allocationSize = 
					arraySize + (length * elementSize) + JvmUtil.OBJECT_ADDRESS_SENSIVITY; // Extra memory for possible aligning
			allocationStartAddress = directMemoryService.allocateMemory(allocationSize); 
			allocationEndAddress = allocationStartAddress + allocationSize;
			
			arrayStartAddress = allocationStartAddress;
			long addressMod = arrayStartAddress % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
			if (addressMod != 0) {
				arrayStartAddress += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - addressMod);
			}
			
			sampleArray = (A) Array.newInstance(JvmUtil.primitiveTypeOf(elementType), 0);
			
			int arrayHeaderSize = JvmUtil.getArrayHeaderSize();
			arrayIndexScale = JvmUtil.arrayIndexScale(elementType);
			arrayIndexStartAddress = arrayStartAddress + JvmUtil.arrayBaseOffset(elementType);
	
			// Copy sample array header to object pool array header
			for (int i = 0; i < arrayHeaderSize; i++) {
				directMemoryService.putByte(arrayStartAddress + i, directMemoryService.getByte(sampleArray, i));
			}
			
			// Set length of array object pool array
			JvmUtil.setArrayLength(arrayStartAddress, elementType, length);

			init();
			
			makeAvaiable();
		}
		catch (IllegalArgumentException e) {
			throw e;
		}
		catch (Throwable t) {
			logger.error("Error occured while initializing \"PrimitiveTypeArrayOffHeapPool\"", t);
			throw new IllegalStateException(t);
		}		
	}

	@Override
	public DeeplyForkableArrayOffHeapPool<T, A, ArrayOffHeapPoolCreateParameter<T>> fork() {
		return 
				new PrimitiveTypeArrayOffHeapPool<T, A>(
							getElementType(), 
							length, 
							getDirectMemoryService());
	}

}
