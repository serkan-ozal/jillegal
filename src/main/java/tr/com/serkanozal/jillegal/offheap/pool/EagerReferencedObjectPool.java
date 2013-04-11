/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import java.lang.reflect.Array;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.SequentialObjectPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public class EagerReferencedObjectPool<T> extends BaseOffHeapPool<T, SequentialObjectPoolCreateParameter<T>> {

	private long objectCount;
	private long objectSize;
	private long arraySize;
	private int currentArrayIndex;
	private long allocatedAddress;
	private T sampleObject;
	private T[] sampleArray;
	private T[] objectArray;
	private long sampleObjectAddress;
	private long addressLimit;
	
	public EagerReferencedObjectPool(SequentialObjectPoolCreateParameter<T> parameter) {
		this(parameter.getElementType(), parameter.getObjectCount(), parameter.getDirectMemoryService());
	}
	
	public EagerReferencedObjectPool(Class<T> clazz, long objectCount, DirectMemoryService directMemoryService) {
		super(clazz, directMemoryService);
		init(clazz, objectCount, directMemoryService);
	}
	
	@SuppressWarnings("unchecked")
	protected synchronized void init() {
		this.currentArrayIndex = 0;
		
		int arrayHeaderSize = JvmUtil.getArrayHeaderSize();
		long arrayIndexStartAddress = allocatedAddress + JvmUtil.arrayBaseOffset(elementType);
		long objStartAddress = allocatedAddress + JvmUtil.sizeOfArray(elementType, objectCount);
		long diffBetweenArrayAndObjectStartAddresses = objStartAddress - allocatedAddress;
		long addressMod = diffBetweenArrayAndObjectStartAddresses % JvmUtil.getAddressSize();
		if (addressMod != 0) {
			objStartAddress += (JvmUtil.getAddressSize() - addressMod);
		}

		for (int i = 0; i < arrayHeaderSize; i++) {
			directMemoryService.putByte(allocatedAddress + i, directMemoryService.getByte(sampleArray, i));
		}
		directMemoryService.putInt(arrayIndexStartAddress - JvmUtil.arrayLengthSize(), (int)objectCount);

		for (long l = 0; l < objectCount; l++) {
			//System.out.println(Long.toHexString((((objStartAddress + (l * objectSize) + arrayHeaderSize + 8)))));
			//System.out.println(Long.toHexString(((int)((objStartAddress + (l * objectSize) + arrayHeaderSize + 8)) >> 3) << 3));
			directMemoryService.putLong(arrayIndexStartAddress + (l * 4), (int) ((objStartAddress + (l * objectSize))) >> 3);
		}

		for (long l = 0; l < objectCount; l++) {
			directMemoryService.copyMemory(sampleObjectAddress, objStartAddress + (l * objectSize), objectSize);
		}
		
		this.objectArray = (T[]) directMemoryService.getObject(allocatedAddress);

		for (int i = 0; i < 64; i++) {
			System.out.print(String.format("%02x ", directMemoryService.getByte(objectArray, i)));
			if ((i + 1) % 16 == 0) {
				System.out.println();
			}
		}
	}
	
	public long getObjectCount() {
		return objectCount;
	}
	
	@Override
	public synchronized T newObject() {
		if (currentArrayIndex >= addressLimit) {
			return null;
		}
		return objectArray[currentArrayIndex++];
	}
	
	@Override
	public synchronized T getObject(long objectIndex) {
		if (objectIndex < 0 || objectIndex > objectCount) {
			return null;
		}	
		return objectArray[(int)objectIndex];
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
		this.arraySize = JvmUtil.sizeOfArray(clazz, objectCount);
		this.allocatedAddress = directMemoryService.allocateMemory(arraySize + (objectCount * objectSize));
		this.addressLimit = allocatedAddress + arraySize + (objectCount * objectSize);
		try {
			this.sampleObject = (T) clazz.newInstance();
		} 
		catch (Exception e) {
			e.printStackTrace();
			this.sampleObject = (T) directMemoryService.allocateInstance(clazz);
		} 
		this.sampleArray = (T[]) Array.newInstance(clazz, 0);
		this.sampleObjectAddress = directMemoryService.addressOf(sampleObject);
		
		init();
	}
	
}
