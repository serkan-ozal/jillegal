/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.StringOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.pool.ContentAwareOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableStringOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.StringOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapConstants;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public class DefaultStringOffHeapPool extends BaseOffHeapPool<String, StringOffHeapPoolCreateParameter> 
		implements 	StringOffHeapPool, 
					DeeplyForkableStringOffHeapPool, 
					ContentAwareOffHeapPool<String, StringOffHeapPoolCreateParameter> {

	protected static final byte STRING_SEGMENT_COUNT_AT_AN_IN_USE_BLOCK = 8;
	protected static final byte STRING_SEGMENT_SIZE = 16;
	protected static final byte SEGMENT_BLOCK_IS_AVAILABLE = 0;
	protected static final byte SEGMENT_BLOCK_IS_IN_USE = 1;
	protected static final byte BLOCK_IS_FULL = -1;
	protected static final byte INDEX_NOT_YET_USED = -1;
	protected static final byte INDEX_NOT_AVAILABLE = -2;
	protected static final byte BLOCK_IS_FULL_VALUE = (byte)0xFF;
	
	protected int estimatedStringCount;
	protected int estimatedStringLength;
	protected int charArrayIndexScale;
	protected int charArrayIndexStartOffset;
	protected int valueArrayOffsetInString;
	protected int stringSize;
	protected long allocationStartAddress;
	protected long allocationEndAddress;
	protected long allocationSize;
	protected long stringsStartAddress;
	protected long currentAddress;
	protected long totalSegmentCount;
	protected long inUseBlockCount;
	protected long inUseBlockAddress;
	protected long currentSegmentIndex;
	protected long currentSegmentBlockIndex;
	protected int sampleHeader;
	protected byte fullValueOfLastBlock;
	protected volatile boolean full;
	
	public DefaultStringOffHeapPool() {
		this(	OffHeapConstants.DEFAULT_ESTIMATED_STRING_COUNT, 
				OffHeapConstants.DEFAULT_ESTIMATED_STRING_LENGTH);
	}
	
	public DefaultStringOffHeapPool(StringOffHeapPoolCreateParameter parameter) {
		this(parameter.getEstimatedStringCount(), parameter.getEstimatedStringLength());
	}
	
	public DefaultStringOffHeapPool(int estimatedStringCount, int estimatedStringLength) {
		super(String.class);
		init(estimatedStringCount, estimatedStringLength);
	}
	
	@Override
	protected void init() {
		super.init();
		currentAddress = stringsStartAddress;
		full = false;
		currentSegmentIndex = INDEX_NOT_YET_USED;
		currentSegmentBlockIndex = INDEX_NOT_YET_USED;
		directMemoryService.setMemory(inUseBlockAddress, inUseBlockCount, (byte)0);
	}
	
	@SuppressWarnings("deprecation")
	protected void init(int estimatedStringCount, int estimatedStringLength) {
		try {
			this.estimatedStringCount = estimatedStringCount;
			this.estimatedStringLength = estimatedStringLength;
			
			charArrayIndexScale = JvmUtil.arrayIndexScale(char.class);
			charArrayIndexStartOffset = JvmUtil.arrayBaseOffset(char.class);
			valueArrayOffsetInString = JvmUtil.getUnsafe().fieldOffset(String.class.getDeclaredField("value"));
			stringSize = (int) JvmUtil.sizeOf(String.class);
			int estimatedStringSize = (int) (stringSize + JvmUtil.sizeOfArray(char.class, estimatedStringLength));
			int estimatedSizeMod = estimatedStringSize % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
			if (estimatedSizeMod != 0) {
				estimatedStringSize += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - estimatedSizeMod);
			}
			allocationSize = (estimatedStringSize * estimatedStringCount) + JvmUtil.OBJECT_ADDRESS_SENSIVITY; // Extra memory for possible aligning
			allocationStartAddress = directMemoryService.allocateMemory(allocationSize); 
			allocationEndAddress = allocationStartAddress + allocationSize;
			// Allocated objects must start aligned as address size from start address of allocated address
			stringsStartAddress = allocationStartAddress;
			long addressMod = stringsStartAddress % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
			if (addressMod != 0) {
				stringsStartAddress += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - addressMod);
			}
			currentAddress = stringsStartAddress;
			totalSegmentCount = allocationSize / STRING_SEGMENT_SIZE;
			long segmentCountMod = allocationSize % STRING_SEGMENT_SIZE;
			if (segmentCountMod != 0) {
				totalSegmentCount++;
			}
			inUseBlockCount = totalSegmentCount / STRING_SEGMENT_COUNT_AT_AN_IN_USE_BLOCK;
			long blockCountMod = totalSegmentCount % STRING_SEGMENT_COUNT_AT_AN_IN_USE_BLOCK;
			if (blockCountMod != 0) {
				inUseBlockCount++;
				fullValueOfLastBlock = (byte)(Math.pow(2, blockCountMod) - 1);
			}
			else {
				fullValueOfLastBlock = BLOCK_IS_FULL_VALUE;
			}
			inUseBlockAddress = directMemoryService.allocateMemory(inUseBlockCount);
			sampleHeader = directMemoryService.getInt(new String(), 0L);

			init();
			makeAvaiable();
		}
		catch (Throwable t) {
			logger.error("Error occured while initializing \"StringOffHeapPool\"", t);
			throw new IllegalStateException(t);
		}	
	}
	
	@Override
	public Class<String> getElementType() {
		return String.class;
	}
	
	protected int calculateSegmentedStringSize(String str) {
		char[] valueArray = (char[]) directMemoryService.getObject(str, valueArrayOffsetInString);
		int valueArraySize = charArrayIndexStartOffset + (charArrayIndexScale * valueArray.length);
		int segmentedStringSize = stringSize + valueArraySize + JvmUtil.OBJECT_ADDRESS_SENSIVITY; // Extra memory for possible aligning
		int segmentSizeMod = segmentedStringSize % STRING_SEGMENT_SIZE;
		if (segmentSizeMod != 0) {
			segmentedStringSize += (STRING_SEGMENT_SIZE - segmentSizeMod);
		}
		return segmentedStringSize;
	}
	
	protected int calculateSegmentedStringSize(long strAddress) {
		char[] valueArray = 
				(char[]) 
					directMemoryService.getObject(
						JvmUtil.toNativeAddress(	
								directMemoryService.getAddress(
										strAddress + valueArrayOffsetInString)));
		int valueArraySize = charArrayIndexStartOffset + (charArrayIndexScale * valueArray.length);
		int segmentedStringSize = stringSize + valueArraySize + JvmUtil.OBJECT_ADDRESS_SENSIVITY; // Extra memory for possible aligning
		int segmentSizeMod = segmentedStringSize % STRING_SEGMENT_SIZE;
		if (segmentSizeMod != 0) {
			segmentedStringSize += (STRING_SEGMENT_SIZE - segmentSizeMod);
		}
		return segmentedStringSize;
	}
	
	protected int calculateStringSegmentCount(String str) {
		return calculateSegmentedStringSize(str) / STRING_SEGMENT_SIZE;
	}
	
	protected int calculateStringSegmentCount(long strAddress) {
		return calculateSegmentedStringSize(strAddress) / STRING_SEGMENT_SIZE;
	}
	
	protected byte getInUseFromStringSegment(long strSegment, int segmentCount) {
		if (strSegment + segmentCount >= totalSegmentCount) {
			return BLOCK_IS_FULL;
		}
		for (int i = 0; i < segmentCount; i++) {
			long blockIndex = strSegment / STRING_SEGMENT_COUNT_AT_AN_IN_USE_BLOCK;
			byte blockIndexValue = directMemoryService.getByte(inUseBlockAddress + blockIndex);
			if (blockIndexValue == BLOCK_IS_FULL_VALUE) {
				return BLOCK_IS_FULL;
			}
			else {
				byte blockInternalOrder = (byte) (strSegment % STRING_SEGMENT_COUNT_AT_AN_IN_USE_BLOCK);
				if (getBit(blockIndexValue, blockInternalOrder) == 1) {
					return SEGMENT_BLOCK_IS_IN_USE;
				}			
			}
			strSegment++;
		}	
		return SEGMENT_BLOCK_IS_AVAILABLE;
	}
	
	protected byte getInUseFromStringAddress(long strAddress, int segmentCount) {
		return 
			getInUseFromStringSegment(
					(strAddress - stringsStartAddress) / STRING_SEGMENT_SIZE,
					segmentCount);
	}
	
	protected void setUnsetInUseFromStringSegment(long strSegment, int segmentCount, boolean set) {
		if (strSegment + segmentCount >= totalSegmentCount) {
			return;
		}
		for (int i = 0; i < segmentCount; i++) {
			long blockIndex = strSegment / STRING_SEGMENT_COUNT_AT_AN_IN_USE_BLOCK;
			byte blockIndexValue = directMemoryService.getByte(inUseBlockAddress + blockIndex);
			byte blockInternalOrder = (byte) (strSegment % STRING_SEGMENT_COUNT_AT_AN_IN_USE_BLOCK);
			byte newBlockIndexValue = 
					set ? 
						setBit(blockIndexValue, blockInternalOrder) : 
						unsetBit(blockIndexValue, blockInternalOrder);
			directMemoryService.putByte(inUseBlockAddress + blockIndex, newBlockIndexValue);
			strSegment++;
		}	
	}
	
	protected void setUnsetInUseFromStringAddress(long strAddress, int segmentCount, boolean set) {
		setUnsetInUseFromStringSegment(
				(strAddress - stringsStartAddress) / STRING_SEGMENT_SIZE, 
				segmentCount,
				set);
	}
	
	protected void allocateStringFromStringSegment(long strSegment, int segmentCount) {
		setUnsetInUseFromStringSegment(strSegment, segmentCount, true);
	}
	
	protected void freeStringFromStringSegment(long strSegment, int segmentCount) {
		setUnsetInUseFromStringSegment(strSegment, segmentCount, false);
		full = false;
	}
	
	protected void allocateStringFromStringAddress(long strAddress, int segmentCount) {
		setUnsetInUseFromStringAddress(strAddress, segmentCount, true);
	}
	
	protected void freeStringFromStringAddress(long strAddress, int segmentCount) {
		setUnsetInUseFromStringAddress(strAddress, segmentCount, false);
		full = false;
	}
	
	protected boolean nextAvailable(int segmentCount) {
		if (full) {
			return false;
		}
		
		currentSegmentIndex++;
		if (currentSegmentIndex >= segmentCount) {
			currentSegmentIndex = 0;
		}
		currentSegmentBlockIndex = currentSegmentIndex / STRING_SEGMENT_COUNT_AT_AN_IN_USE_BLOCK;
		
		byte segmentBlockInUse = getInUseFromStringSegment(currentSegmentIndex, segmentCount);
		byte blockIndexValue = directMemoryService.getByte(inUseBlockAddress + currentSegmentBlockIndex);
		
		// Current segment index is not available
		if (segmentBlockInUse != SEGMENT_BLOCK_IS_AVAILABLE) {
			boolean backToHead = false;
			long lastSegmentBlockIndex = currentSegmentBlockIndex;
			
			while (segmentBlockInUse != SEGMENT_BLOCK_IS_AVAILABLE) {
				// All blocks are checked but there is no available segment block
				if (backToHead && currentSegmentBlockIndex > lastSegmentBlockIndex) {
					currentSegmentBlockIndex = INDEX_NOT_AVAILABLE;
					currentSegmentIndex = INDEX_NOT_AVAILABLE;
					full = true;
					return false;
				}
				
				// Move to next segment block
				if (	segmentBlockInUse == BLOCK_IS_FULL ||
						(currentSegmentBlockIndex == (inUseBlockCount - 1) && blockIndexValue == fullValueOfLastBlock)) {
					currentSegmentBlockIndex++;
					currentSegmentIndex = currentSegmentBlockIndex * STRING_SEGMENT_COUNT_AT_AN_IN_USE_BLOCK;
				}
				// Move to next segment index
				else if (segmentBlockInUse == SEGMENT_BLOCK_IS_IN_USE) {
					currentSegmentIndex++;
					currentSegmentBlockIndex = currentSegmentIndex / STRING_SEGMENT_COUNT_AT_AN_IN_USE_BLOCK;
				}
				
				// End of segments, back to head
				if (currentSegmentBlockIndex >= inUseBlockCount) {
					currentSegmentBlockIndex = 0;
					currentSegmentIndex = 0;
					backToHead = true;
				}
				
				segmentBlockInUse = getInUseFromStringSegment(currentSegmentIndex, segmentCount);
				blockIndexValue = directMemoryService.getByte(inUseBlockAddress + currentSegmentBlockIndex);
			}	
		}
		
		currentAddress = stringsStartAddress + (currentSegmentIndex * STRING_SEGMENT_SIZE);
		
		currentSegmentIndex += (segmentCount - 1);
		currentSegmentBlockIndex = currentSegmentIndex / STRING_SEGMENT_COUNT_AT_AN_IN_USE_BLOCK;

		return true;
	}
	
	protected synchronized String takeString(String str) {
		long objAddress = directMemoryService.addressOf(str);
		directMemoryService.putInt(objAddress, sampleHeader);
		allocateStringFromStringAddress(objAddress, calculateStringSegmentCount(str));
		return str;
	}
	
	protected synchronized String takeString(long strAddress) {
		String str = (String) directMemoryService.getObject(strAddress);
		directMemoryService.putInt(strAddress, sampleHeader);
		allocateStringFromStringAddress(strAddress, calculateStringSegmentCount(strAddress));
		return str;
	}
	
	protected synchronized long takeStringAsAddress(long strAddress) {
		directMemoryService.putInt(strAddress, sampleHeader);
		allocateStringFromStringAddress(strAddress, calculateStringSegmentCount(strAddress));
		return strAddress;
	}
	
	protected synchronized boolean releaseString(String str) {
		return releaseString(directMemoryService.addressOf(str));
	}
	
	protected synchronized boolean releaseString(long strAddress) {
		if (!isIn(strAddress)) {
			return false;
		}
		// Reset free object
		int stringSegmentedSize = calculateSegmentedStringSize(strAddress);
		directMemoryService.setMemory(strAddress, stringSegmentedSize, (byte)0);
		freeStringFromStringAddress(strAddress, stringSegmentedSize / STRING_SEGMENT_SIZE);
		return true;
	}
	
	protected long allocateStringFromOffHeap(String str) {
		long addressOfStr = directMemoryService.addressOf(str);
		char[] valueArray = (char[]) directMemoryService.getObject(str, valueArrayOffsetInString);
		int valueArraySize = charArrayIndexStartOffset + (charArrayIndexScale * valueArray.length);
		int strSize = stringSize + valueArraySize + JvmUtil.OBJECT_ADDRESS_SENSIVITY; // Extra memory for possible aligning
		
		long addressMod1 = currentAddress % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
		if (addressMod1 != 0) {
			currentAddress += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - addressMod1);
		}
		
		if (currentAddress + strSize > allocationEndAddress) {
			return JvmUtil.NULL;
		}
		
		// Copy string object content to allocated area
		directMemoryService.copyMemory(addressOfStr, currentAddress, strSize);
		
		long valueAddress = currentAddress + stringSize;
		long addressMod2 = valueAddress % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
		if (addressMod2 != 0) {
			valueAddress += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - addressMod2);
		}
		
		// Copy value array in allocated string to allocated char array
		directMemoryService.copyMemory(
				JvmUtil.toNativeAddress(
						directMemoryService.getAddress(addressOfStr + valueArrayOffsetInString)),
				valueAddress, 
				valueArraySize);

		// Now, value array in allocated string points to allocated char array
		directMemoryService.putAddress(
				currentAddress + valueArrayOffsetInString, 
				JvmUtil.toJvmAddress(valueAddress));
		
		return takeStringAsAddress(currentAddress);
	}
	
	@Override
	public synchronized String get(String str) {
		checkAvailability();
		int requiredSegmentCount = calculateStringSegmentCount(str);
		if (!nextAvailable(requiredSegmentCount)) {
			return null;
		}
		long allocatedStrAddress = allocateStringFromOffHeap(str);
		if (allocatedStrAddress == JvmUtil.NULL) {
			return null;
		}
		else {
			return directMemoryService.getObject(allocatedStrAddress);
		}
	}
	
	@Override
	public synchronized long getAsAddress(String str) {
		checkAvailability();
		int requiredSegmentCount = calculateStringSegmentCount(str);
		if (!nextAvailable(requiredSegmentCount)) {
			return JvmUtil.NULL;
		}
		return allocateStringFromOffHeap(str);
	}
	
	@Override
	public boolean isMine(String str) {
		checkAvailability();
		if (str == null) {
			return false;
		}
		else {
			return isMine(directMemoryService.addressOf(str));
		}	
	}
	
	@Override
	public boolean isMine(long address) {
		checkAvailability();
		return isIn(address);
	}
	
	@Override
	public synchronized boolean free(String str) {
		checkAvailability();
		if (str == null) {
			return false;
		}
		return releaseString(str);
	}
	
	@Override
	public synchronized boolean freeFromAddress(long strAddress) {
		checkAvailability();
		return releaseString(strAddress);
	}

	@Override
	public synchronized void init(StringOffHeapPoolCreateParameter parameter) {
		init(parameter.getEstimatedStringCount(), parameter.getEstimatedStringLength());
	}
	
	@Override
	public synchronized void reset() {
		init();
		makeAvaiable();
	}
	
	@Override
	public synchronized void free() {
		checkAvailability();
		directMemoryService.freeMemory(inUseBlockAddress);
		directMemoryService.freeMemory(allocationStartAddress);
		makeUnavaiable();
	}
	
	@Override
	public DeeplyForkableStringOffHeapPool fork() {
		return 
			new DefaultStringOffHeapPool(
						estimatedStringCount, 
						estimatedStringLength);
	}
	
}
