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

	protected static final byte OBJECT_COUNT_AT_AN_IN_USE_BLOCK = 8;
	protected static final byte OBJECT_IS_AVAILABLE = 0;
	protected static final byte OBJECT_IS_IN_USE = 1;
	protected static final byte BLOCK_IS_FULL = -1;
	protected static final byte INDEX_NOT_YET_USED = -1;
	protected static final byte INDEX_NOT_AVAILABLE = -2;
	protected static final byte BLOCK_IS_FULL_VALUE = (byte)0xFF;
	
	protected long objectSize;
	protected long objectCount;
	protected long inUseBlockCount;
	protected long currentAddress;
	protected long inUseBlockAddress;
	protected long currentIndex;
	protected long currentBlockIndex;
	protected long objectsStartAddress;
	protected long objectsEndAddress;
	protected T sampleObject;
	protected long offHeapSampleObjectAddress;
	protected int sampleHeader;
	protected byte fullValueOfLastBlock;
	protected volatile boolean full;
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
		full = false;
		currentIndex = INDEX_NOT_YET_USED;
		currentBlockIndex = INDEX_NOT_YET_USED;
		directMemoryService.setMemory(inUseBlockAddress, inUseBlockCount, (byte)0);
	}
	
	protected void init(Class<T> elementType, long objectCount, 
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
		this.inUseBlockCount = objectCount / OBJECT_COUNT_AT_AN_IN_USE_BLOCK;
		long blockCountMod = objectCount % OBJECT_COUNT_AT_AN_IN_USE_BLOCK;
		if (blockCountMod != 0) {
			this.inUseBlockCount++;
			this.fullValueOfLastBlock = (byte)(Math.pow(2, blockCountMod) - 1);
		}
		else {
			this.fullValueOfLastBlock = BLOCK_IS_FULL_VALUE;
		}
		this.inUseBlockAddress = directMemoryService.allocateMemory(inUseBlockCount);
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
	 */
	
	protected byte getInUseFromObjectIndex(long objIndex) {
		long blockIndex = objIndex / OBJECT_COUNT_AT_AN_IN_USE_BLOCK;
		byte blockIndexValue = directMemoryService.getByte(inUseBlockAddress + blockIndex);
		if (blockIndexValue == BLOCK_IS_FULL_VALUE) {
			return BLOCK_IS_FULL;
		}
		else {
			byte blockInternalOrder = (byte) (objIndex % OBJECT_COUNT_AT_AN_IN_USE_BLOCK);
			return
				getBit(blockIndexValue, blockInternalOrder) == 1 ? 
						OBJECT_IS_IN_USE : OBJECT_IS_AVAILABLE;
		}	
	}
	
	protected byte getInUseFromObjectAddress(long objAddress) {
		return getInUseFromObjectIndex((objAddress - objectsStartAddress) / objectSize);
	}
	
	protected void setUnsetInUseFromObjectIndex(long objIndex, boolean set) {
		long blockIndex = objIndex / OBJECT_COUNT_AT_AN_IN_USE_BLOCK;
		byte blockIndexValue = directMemoryService.getByte(inUseBlockAddress + blockIndex);
		byte blockInternalOrder = (byte) (objIndex % OBJECT_COUNT_AT_AN_IN_USE_BLOCK);
		byte newBlockIndexValue = 
				set ? 
					setBit(blockIndexValue, blockInternalOrder) : 
					unsetBit(blockIndexValue, blockInternalOrder);
		directMemoryService.putByte(inUseBlockAddress + blockIndex, newBlockIndexValue);
	}
	
	protected void setUnsetInUseFromObjectAddress(long objAddress, boolean set) {
		setUnsetInUseFromObjectIndex((objAddress - objectsStartAddress) / objectSize, set);
	}
	
	protected void allocateObjectFromObjectIndex(long objIndex) {
		setUnsetInUseFromObjectIndex(objIndex, true);
	}
	
	protected void freeObjectFromObjectIndex(long objIndex) {
		setUnsetInUseFromObjectIndex(objIndex, false);
		full = false;
	}
	
	protected void allocateObjectFromObjectAddress(long objAddress) {
		setUnsetInUseFromObjectAddress(objAddress, true);
	}
	
	protected void freeObjectFromObjectAddress(long objAddress) {
		setUnsetInUseFromObjectAddress(objAddress, false);
		full = false;
	}
	
	protected boolean nextAvailable() {
		if (full) {
			return false;
		}
		currentIndex++;
		if (currentIndex >= objectCount) {
			currentIndex = 0;
		}
		byte objectInUse = getInUseFromObjectIndex(currentIndex);
		// Object on current index is not available
		if (objectInUse != OBJECT_IS_AVAILABLE) {
			// Current object is not available, so search in current block for available one
			if (objectInUse != BLOCK_IS_FULL) {
				byte blockIndexValue = directMemoryService.getByte(inUseBlockAddress + currentBlockIndex);
				// Check objects in block
				for (int i = 0; i < OBJECT_COUNT_AT_AN_IN_USE_BLOCK; i++) {
					// If current object is not in use, use it
					if (((blockIndexValue >> i) & 0x01) == 0) {
						break;
					}
					currentIndex++;
				}
			}
			else {
				currentBlockIndex = currentIndex / OBJECT_COUNT_AT_AN_IN_USE_BLOCK;
				byte blockIndexValue = directMemoryService.getByte(inUseBlockAddress + currentBlockIndex);
				long checkedBlockCount;
				// Search for non-full block
				for (	checkedBlockCount = 0; 
						blockIndexValue == BLOCK_IS_FULL_VALUE && checkedBlockCount < inUseBlockCount; 
						checkedBlockCount++) {
					currentBlockIndex++;
					if (currentBlockIndex >= inUseBlockCount) {
						currentBlockIndex = 0;
					}
					currentIndex = currentBlockIndex * OBJECT_COUNT_AT_AN_IN_USE_BLOCK;
					blockIndexValue = directMemoryService.getByte(inUseBlockAddress + currentBlockIndex);
				}
				// All blocks are checked but there is no non-full block
				if (	checkedBlockCount >=  inUseBlockCount || 
						(currentBlockIndex == (inUseBlockCount - 1) && blockIndexValue == fullValueOfLastBlock)) {
					currentIndex = INDEX_NOT_AVAILABLE;
					currentBlockIndex = INDEX_NOT_AVAILABLE;
					full = true;
					return false;
				}
				// A non-full block found, check free object in block
				for (int i = 0; i < OBJECT_COUNT_AT_AN_IN_USE_BLOCK; i++) {
					// If current object is not in use, use it
					if (((blockIndexValue >> i) & 0x01) == 0) {
						break;
					}
					currentIndex++;
				}
			}	
		}
		currentAddress = objectsStartAddress + (currentIndex * objectSize);
		return true;
	}

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
	
	protected synchronized T takeObject(T obj) {
		long objAddress = directMemoryService.addressOf(obj);
		obj = super.processObject(obj);
		directMemoryService.putInt(objAddress, sampleHeader);
		allocateObjectFromObjectAddress(objAddress);
		return obj;
	}
	
	@SuppressWarnings("unchecked")
	protected synchronized T takeObject(long objAddress) {
		T obj = (T) directMemoryService.getObject(objAddress);
		obj = super.processObject(obj);
		directMemoryService.putInt(objAddress, sampleHeader);
		allocateObjectFromObjectAddress(objAddress);
		return obj;
	}
	
	protected synchronized long takeObjectAsAddress(long objAddress) {
		long address = super.processObject(objAddress);
		directMemoryService.putInt(objAddress, sampleHeader);
		allocateObjectFromObjectAddress(address);
		return address;
	}
	
	protected synchronized boolean releaseObject(T obj) {
		return releaseObject(directMemoryService.addressOf(obj));
	}
	
	protected synchronized boolean releaseObject(long objAddress) {
		if (!isIn(objAddress)) {
			return false;
		}
		// Reset free object
		directMemoryService.putInt(objAddress, 0);
		directMemoryService.copyMemory(offHeapSampleObjectAddress + 4, objAddress + 4, objectSize - 4);
		freeObjectFromObjectAddress(objAddress);
		return true;
	}
	
	@Override
	public synchronized void free() {
		checkAvailability();
		directMemoryService.freeMemory(offHeapSampleObjectAddress);
		directMemoryService.freeMemory(inUseBlockAddress);
		directMemoryService.freeMemory(allocationStartAddress);
		makeUnavaiable();
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

	// Get and copy class pointer to current object's class pointer field. 
	// Address of class could be changed by GC at "Compact" phase.
	protected long updateClassPointerOfObject(long address) {
		return jvmAwareClassPointerUpdater.updateClassPointerOfObject(address);
	}
	*/
	
}
