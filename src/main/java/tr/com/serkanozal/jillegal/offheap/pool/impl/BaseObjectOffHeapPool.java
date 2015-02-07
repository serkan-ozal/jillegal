/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import tr.com.serkanozal.jillegal.offheap.OffHeapAwareObject;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.pool.ContentAwareOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public abstract class BaseObjectOffHeapPool<T, P extends OffHeapPoolCreateParameter<T>> 
		extends BaseOffHeapPool<T, P> implements ObjectOffHeapPool<T, P>, ContentAwareOffHeapPool<T, P> {

	protected static final byte OBJECT_IS_AVAILABLE = 0;
	protected static final byte OBJECT_IS_IN_USE = 1;
	protected static final byte INDEX_NOT_YET_USED = -1;
	protected static final byte INDEX_NOT_AVAILABLE = -2;
	
	protected long objectSize;
	protected long objectCount;
	protected long inUseBlockCount;
	protected long usedObjectCount;
	protected volatile long currentAddress;
	protected long inUseBlockAddress;
	protected volatile long currentIndex;
	protected long objectsStartAddress;
	protected long objectsEndAddress;
	protected T sampleObject;
	protected long offHeapSampleObjectAddress;
	protected long sampleHeader;
	protected volatile boolean full;

	public BaseObjectOffHeapPool(Class<T> objectType) {
		super(objectType);
		offHeapService.makeOffHeapable(objectType);
	}
	
	public BaseObjectOffHeapPool(Class<T> objectType, DirectMemoryService directMemoryService) {
		super(objectType, directMemoryService);
		offHeapService.makeOffHeapable(objectType);
	}
	
	@Override
	protected void init() {
		super.init();
		full = false;
		currentIndex = INDEX_NOT_YET_USED;
		directMemoryService.setMemory(inUseBlockAddress, inUseBlockCount, (byte) 0x00);
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
		this.usedObjectCount = 0;
		this.directMemoryService = directMemoryService;
		
		inUseBlockCount = objectCount;
		inUseBlockAddress = directMemoryService.allocateMemory(inUseBlockCount);
		sampleObject = JvmUtil.getSampleInstance(elementType);
		if (sampleObject == null) {
			throw new IllegalStateException("Unable to create a sample object for class " + elementType.getName());
		}
		sampleHeader = directMemoryService.getLong(sampleObject, 0L);
		objectSize = directMemoryService.sizeOfObject(sampleObject);
		offHeapSampleObjectAddress = directMemoryService.allocateMemory(objectSize);
		directMemoryService.copyMemory(sampleObject, 0L, null, offHeapSampleObjectAddress, objectSize);
		/*
		for (int i = 0; i < objectSize; i += JvmUtil.LONG_SIZE) {
			directMemoryService.putLong(offHeapSampleObjectAddress + i, directMemoryService.getLong(sampleObject, i));
		}
		*/
		/*
		for (int i = 0; i < objectSize; i++) {
			directMemoryService.putByte(offHeapSampleObjectAddress + i, directMemoryService.getByte(sampleObject, i));
		}
		*/
		// directMemoryService.copyMemory(directMemoryService.addressOf(sampleObject), offHeapSampleObjectAddress, objectSize);
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
		return directMemoryService.getByteVolatile(inUseBlockAddress + objIndex);	
	}
	
	protected void resetObject(long objAddress) {
		directMemoryService.copyMemory(offHeapSampleObjectAddress, objAddress, objectSize);
	}
	
	protected byte getInUseFromObjectAddress(long objAddress) {
		return getInUseFromObjectIndex((objAddress - objectsStartAddress) / objectSize);
	}
	
	protected void setUnsetInUseFromObjectIndex(long objIndex, boolean set) {
		directMemoryService.putByteVolatile(inUseBlockAddress + objIndex, 
				set ? OBJECT_IS_IN_USE : OBJECT_IS_AVAILABLE);
		if (full && !set) {
			currentIndex = objIndex - 1;
		}
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
		if (full || currentIndex == INDEX_NOT_AVAILABLE) {
			return false;
		}
		
		currentIndex++;
		if (currentIndex >= objectCount) {
			currentIndex = 0;
		}
		
		byte objectInUse = getInUseFromObjectIndex(currentIndex);
		int checkedBlockCount = 0;
		while (checkedBlockCount < objectCount) {
			if (objectInUse == OBJECT_IS_AVAILABLE) {
				break;
			}
			currentIndex++;
			if (currentIndex >= objectCount) {
				currentIndex = 0;
			}
			objectInUse = getInUseFromObjectIndex(currentIndex);
			checkedBlockCount++;
		}
		
		if (objectInUse != OBJECT_IS_AVAILABLE) {
			currentIndex = INDEX_NOT_AVAILABLE;
			full = true;
			return false;
		}	
		
		if (currentIndex < 0 || currentIndex >= objectCount) {
			logger.error("Invalid index for available element type " + elementType.getName() + ": " + currentIndex);
			return false;
		}

		currentAddress = objectsStartAddress + (currentIndex * objectSize);

		return true;
	}
	
	@Override
	public boolean isFull() {
		return full;
	}
	
	@Override
	public boolean isEmpty() {
		return usedObjectCount == 0;
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
		resetObject(objAddress);
		directMemoryService.putLong(objAddress, sampleHeader);
		obj = super.processObject(obj);
		allocateObjectFromObjectAddress(objAddress);
		usedObjectCount++;
		if (obj instanceof OffHeapAwareObject) {
			((OffHeapAwareObject) obj).onGet(offHeapService, directMemoryService);
		}
		return obj;
	}
	
	@SuppressWarnings("unchecked")
	protected synchronized T takeObject(long objAddress) {
		resetObject(objAddress);
		directMemoryService.putLong(objAddress, sampleHeader);
		T obj = (T) directMemoryService.getObject(objAddress);
		obj = super.processObject(obj);
		allocateObjectFromObjectAddress(objAddress);
		usedObjectCount++;
		if (obj instanceof OffHeapAwareObject) {
			((OffHeapAwareObject) obj).onGet(offHeapService, directMemoryService);
		}
		return obj;
	}
	
	@SuppressWarnings("unchecked")
	protected synchronized long takeObjectAsAddress(long objAddress) {
		resetObject(objAddress);
		directMemoryService.putLong(objAddress, sampleHeader);
		long address = super.processObject(objAddress);
		allocateObjectFromObjectAddress(address);
		T obj = (T) directMemoryService.getObject(objAddress);
		usedObjectCount++;
		if (obj instanceof OffHeapAwareObject) {
			((OffHeapAwareObject) obj).onGet(offHeapService, directMemoryService);
		}
		return address;
	}
	
	protected synchronized boolean releaseObject(T obj) {
		long objAddress = directMemoryService.addressOf(obj);
		if (!isIn(objAddress)) {
			return false;
		}
//		if (getInUseFromObjectAddress(objAddress) != OBJECT_IS_IN_USE) {
//			return false;
//		}
		if (obj instanceof OffHeapAwareObject) {
			((OffHeapAwareObject) obj).onFree(offHeapService, directMemoryService);
		}
		return doReleaseObject(objAddress);
	}
	
	@SuppressWarnings("unchecked")
	protected synchronized boolean releaseObject(long objAddress) {
		if (!isIn(objAddress)) {
			return false;
		}
//		if (getInUseFromObjectAddress(objAddress) != OBJECT_IS_IN_USE) {
//			return false;
//		}
		T obj = (T) directMemoryService.getObject(objAddress);
		if (obj instanceof OffHeapAwareObject) {
			((OffHeapAwareObject) obj).onFree(offHeapService, directMemoryService);
		}
		return doReleaseObject(objAddress);
	}
	
	protected synchronized boolean doReleaseObject(long objAddress) {
		// Reset free object
		resetObject(objAddress); 
		directMemoryService.putLong(objAddress, sampleHeader);
		freeObjectFromObjectAddress(objAddress);
		usedObjectCount--;
		return true;
	}
	
	@Override
	public synchronized void free() {
		checkAvailability();
		// TODO Iterate over all objects and call their "onFree" methods if they are instance of "OffHeapAwareObject"
		directMemoryService.freeMemory(offHeapSampleObjectAddress);
		directMemoryService.freeMemory(inUseBlockAddress);
		directMemoryService.freeMemory(allocationStartAddress);
		usedObjectCount = 0;
		makeUnavaiable();
	}

}
