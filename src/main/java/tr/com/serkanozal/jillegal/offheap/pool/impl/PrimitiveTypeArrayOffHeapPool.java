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
import tr.com.serkanozal.jillegal.offheap.pool.ExplicitArrayOffHeapPool;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public class PrimitiveTypeArrayOffHeapPool<T, A> extends BaseOffHeapPool<T, ArrayOffHeapPoolCreateParameter<T>> 
		implements ExplicitArrayOffHeapPool<T, A, ArrayOffHeapPoolCreateParameter<T>> {

	protected int length;
	protected int elementSize;
	protected long arraySize;
	protected long allocatedAddress;
	protected long arrayIndexStartAddress;
	protected int arrayIndexScale;
	protected A sampleArray;
	protected A objectArray;
	
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
	protected synchronized void init() {
		int arrayHeaderSize = JvmUtil.getArrayHeaderSize();
		arrayIndexScale = JvmUtil.arrayIndexScale(elementType);
		arrayIndexStartAddress = allocatedAddress + JvmUtil.arrayBaseOffset(elementType);

		// Copy sample array header to object pool array header
		for (int i = 0; i < arrayHeaderSize; i++) {
			directMemoryService.putByte(allocatedAddress + i, directMemoryService.getByte(sampleArray, i));
		}
		
		// Set length of array object pool array
		directMemoryService.putInt(arrayIndexStartAddress - JvmUtil.arrayLengthSize(), (int)length);

		this.objectArray = (A) directMemoryService.getObject(allocatedAddress);
	}
	
	@Override
	public int getLength() {
		return length;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getAt(int index) {
		if (index < 0 || index > length) {
			return null;
		}	
		return ((T[])(objectArray))[index];
	}
	
	@Override
	public void setAt(T element, int index) {
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
		return (A)objectArray;
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
		init(parameter.getElementType(), parameter.getLength(), parameter.getDirectMemoryService());
	}
	
	@SuppressWarnings("unchecked")
	protected void init(Class<T> elementType, int length, DirectMemoryService directMemoryService) {
		this.elementType = elementType;
		this.length = length;
		this.directMemoryService = directMemoryService;
		this.elementSize = JvmUtil.sizeOfType(elementType);
		this.arraySize = JvmUtil.sizeOfArray(elementType, length);
		this.allocatedAddress = 
				directMemoryService.allocateMemory(arraySize + (length * elementSize) + 
						JvmUtil.getAddressSize()); // Extra memory for possible aligning
		this.sampleArray = (A) Array.newInstance(JvmUtil.primitiveTypeOf(elementType), 0);
		
		init();
	}

}
