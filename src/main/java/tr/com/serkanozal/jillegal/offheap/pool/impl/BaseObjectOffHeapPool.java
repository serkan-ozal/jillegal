/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.pool.ContentAwareOffHeapPool;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public abstract class BaseObjectOffHeapPool<T, P extends OffHeapPoolCreateParameter<T>> 
		extends BaseOffHeapPool<T, P> implements ContentAwareOffHeapPool<T, P> {

	protected static final int OBJECT_COUNT_AT_IN_USE_BLOCK = 128;
	protected long inUseBlockAddress;
	
	protected int objectCount;
	protected long objectSize;
	protected long currentAddress;
	protected long objectsStartAddress;
	protected long objectsEndAddress;
	protected T sampleObject;
	protected long offHeapSampleObjectAddress;
	protected int sampleHeader;
	/*
	protected long classPointerAddress;
	protected long classPointerOffset;
	protected long classPointerSize;
	protected JvmAwareClassPointerUpdater jvmAwareClassPointerUpdater;
	*/
	
	public BaseObjectOffHeapPool(Class<T> objectType) {
		super(objectType);
	}
	
	public BaseObjectOffHeapPool(Class<T> objectType, DirectMemoryService directMemoryService) {
		super(objectType, directMemoryService);
	}
	
	@Override
	protected void init() {
		super.init();
	}
	
	protected void init(Class<T> elementType, int objectCount, 
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType, 
			DirectMemoryService directMemoryService) {
		super.init(elementType, allocateNonPrimitiveFieldsAtOffHeapConfigType, directMemoryService);
		
		if (elementType.isAnnotation()) {
			throw new IllegalArgumentException("Annotation class " + "(" + elementType.getName() + ")" + 
											   " is not supported !");
		}
		if (elementType.isInterface()) {
			throw new IllegalArgumentException("Interface class " + "(" + elementType.getName() + ")" + 
											   " is not supported !");
		}
		if (elementType.isAnonymousClass()) {
			throw new IllegalArgumentException("Anonymous class " + "(" + elementType.getName() + ")" + 
											   " is not supported !");
		}
		this.elementType = elementType;
		this.objectCount = objectCount;
		this.directMemoryService = directMemoryService;
		this.sampleObject = JvmUtil.getSampleInstance(elementType);
		if (sampleObject == null) {
			throw new IllegalStateException("Unable to create a sample object for class " + elementType.getName());
		}
		this.sampleHeader = directMemoryService.getInt(sampleObject, 0L);
		long address = directMemoryService.addressOf(sampleObject);
		this.objectSize = directMemoryService.sizeOfObject(sampleObject);
		this.offHeapSampleObjectAddress = directMemoryService.allocateMemory(objectSize);
		directMemoryService.copyMemory(address, offHeapSampleObjectAddress, objectSize);
		/*
		this.classPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
		this.classPointerOffset = JvmUtil.getClassDefPointerOffsetInObject();
		this.classPointerSize = JvmUtil.getReferenceSize();
		switch (JvmUtil.getAddressSize()) {
	        case JvmUtil.SIZE_32_BIT:
	        	jvmAwareClassPointerUpdater = new Address32BitJvmClassPointerUpdater();
	            break;
	        case JvmUtil.SIZE_64_BIT:
	        	int referenceSize = JvmUtil.getReferenceSize();
	        	switch (referenceSize) {
	             	case JvmUtil.ADDRESSING_4_BYTE:   
	             		jvmAwareClassPointerUpdater = new Address64BitWithCompressedOopsJvmClassPointerUpdater();
	             		break;
	             	case JvmUtil.ADDRESSING_8_BYTE:
	             		jvmAwareClassPointerUpdater = new Address64BitWithoutCompressedOopsJvmClassPointerUpdater();
	             		break;
	             	default:    
	                    throw new AssertionError("Unsupported reference size: " + referenceSize);
	        	}
	        	break;    
	        default:
	            throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());
		}
		*/
	}
	
	protected byte getBit(byte value, byte bit) {
		return (byte) ((value & (1 << bit)) == 0 ? 0 : 1);
	}
	
	protected byte setBit(byte value, byte bit) {
		return (byte) (value | (1 << bit));
	}
	
	protected byte unsetBit(byte value, byte bit) {
		return (byte) (value & (~(1 << bit)));
	}
	
	protected boolean getInUseBit(long objAddress) {
		long objIndex = (objAddress - objectsStartAddress) / objectSize;
		long blockOrder = objIndex / OBJECT_COUNT_AT_IN_USE_BLOCK;
		long blockIndex = blockOrder / 8;
		byte blockIndexValue = directMemoryService.getByte(inUseBlockAddress + blockIndex);
		byte blockInternalOrder = (byte) (blockOrder % 8);
		return getBit(blockIndexValue, blockInternalOrder) == 1;
	}
	
	protected void setUnsetInUseBit(long objAddress, boolean set) {
		long objIndex = (objAddress - objectsStartAddress) / objectSize;
		long blockOrder = objIndex / OBJECT_COUNT_AT_IN_USE_BLOCK;
		long blockIndex = blockOrder / 8;
		byte blockIndexValue = directMemoryService.getByte(inUseBlockAddress + blockIndex);
		byte blockInternalOrder = (byte) (blockOrder % 8);
		byte newBlockIndexValue = 
				set ? 
					setBit(blockIndexValue, blockInternalOrder) : 
					unsetBit(blockIndexValue, blockInternalOrder);
		directMemoryService.putByte(inUseBlockAddress + blockIndex, newBlockIndexValue);
	}
	
	/*
	 * 1. Get object index from its address like "(objAddress - allocationStartAddress) / objectSize"
	 * 
	 * 2. Get block order from object index like "objectIndex / OBJECT_COUNT_AT_IN_USE_BLOCK"
	 * 
	 * 3. Since every byte contains 8 block information (each one is represented by a bit),
	 *    block index can be calculated like "blockIndex = blockOrder / 8"
	 *    
	 * 4. Read block index value like "directMemoryService.getByte(inUseBlockAddress + blockIndex)"
	 * 
	 * 5. Calculate block internal order like "blockOrder % 8"
	 * 
	 * 6. Get block in-use bit like "getBit(blockIndexValue, blockInternalOrder)"
	 * 
	 * 		int getBit(byte value, byte bit) {
	 * 			if (bit == 7) {
	 * 				return (value < 0) ? 1 : 0;
	 * 			}
	 * 			else {
	 *				return (value & (1 << bit)) == 0 ? 0 : 1;
	 *			}
	 * 		}
	 */
	
	@Override
	public boolean isMine(T element) {
		if (element == null) {
			return false;
		}
		else {
			return isMine(directMemoryService.addressOf(element));
		}	
	}
	
	@Override
	public boolean isMine(long address) {
		return isIn(address);
	}
	
	protected synchronized T processObject(T obj) {
		directMemoryService.putInt(obj, 0L, sampleHeader);
		return super.processObject(obj);
	}
	
	protected synchronized long processObject(long objAddress) {
		directMemoryService.putInt(objAddress, sampleHeader);
		return super.processObject(objAddress);
	}
	
	/*
	protected boolean checkAndRefreshIfClassPointerOfObjectUpdated() {
		long currentClassPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
		if (currentClassPointerAddress != classPointerAddress) {
			classPointerAddress = currentClassPointerAddress;
			return true;
		}
		else {
			return false;
		}
	}
	
	protected interface JvmAwareClassPointerUpdater {
		
		long updateClassPointerOfObject(long address);
	
	}
	
	protected class Address32BitJvmClassPointerUpdater implements JvmAwareClassPointerUpdater {

		@Override
		public long updateClassPointerOfObject(long address) {
			classPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
			directMemoryService.putAsIntAddress(address + classPointerOffset, classPointerAddress);
			return address;
		}
		
	}
	
	protected class Address64BitWithoutCompressedOopsJvmClassPointerUpdater implements JvmAwareClassPointerUpdater {

		@Override
		public long updateClassPointerOfObject(long address) {
			classPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
			directMemoryService.putAsIntAddress(address + classPointerOffset, classPointerAddress);
			return address;
		}
		
	}
	
	protected class Address64BitWithCompressedOopsJvmClassPointerUpdater implements JvmAwareClassPointerUpdater {

		@Override
		public long updateClassPointerOfObject(long address) {
			classPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
			directMemoryService.putLong(address + classPointerOffset, classPointerAddress);
			return address;
		}
		
	}
	*/
	
	/**
	 * Get and copy class pointer to current object's class pointer field. 
	 * Address of class could be changed by GC at "Compact" phase.
	 * 
	 * @param address
	 */
	/*
	protected long updateClassPointerOfObject(long address) {
		return jvmAwareClassPointerUpdater.updateClassPointerOfObject(address);
	}
	*/
	
}
