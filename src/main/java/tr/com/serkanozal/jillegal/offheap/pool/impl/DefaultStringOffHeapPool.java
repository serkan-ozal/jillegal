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

@SuppressWarnings( { "restriction" } )
public class DefaultStringOffHeapPool extends BaseOffHeapPool<String, StringOffHeapPoolCreateParameter> 
		implements 	StringOffHeapPool, 
					DeeplyForkableStringOffHeapPool, 
					ContentAwareOffHeapPool<String, StringOffHeapPoolCreateParameter> {

	protected static final byte STRING_SEGMENT_SIZE = 16;
	protected static final byte SEGMENT_IS_AVAILABLE = 0;
	protected static final byte SEGMENT_IS_IN_USE = 1;
	protected static final byte SEGMENT_BLOCK_IS_AVAILABLE = 0;
	protected static final byte SEGMENT_BLOCK_IS_NOT_AVAILABLE = 1;
	protected static final byte INDEX_NOT_YET_USED = -1;
	protected static final byte INDEX_NOT_AVAILABLE = -2;
	
	protected int estimatedStringCount;
	protected int estimatedStringLength;
	protected int charArrayIndexScale;
	protected int charArrayIndexStartOffset;
	protected int valueArrayOffsetInString;
	protected int stringSize;
	protected long stringsStartAddress;
	protected volatile long currentAddress;
	protected long totalSegmentCount;
	protected volatile long usedSegmentCount;
	protected long inUseSegmentAddress;
	protected volatile long currentSegmentIndex;
	protected String sampleStr;
	protected long sampleHeader;
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
		//directMemoryService.setMemory(inUseSegmentAddress, totalSegmentCount, (byte) 0x00);
	}
	
	@SuppressWarnings({ "deprecation" })
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
			totalSegmentCount = (allocationEndAddress - stringsStartAddress) / STRING_SEGMENT_SIZE;
			usedSegmentCount = 0;
			inUseSegmentAddress = directMemoryService.allocateMemory(totalSegmentCount);
			sampleStr = "";
			sampleHeader = directMemoryService.getLong(sampleStr, 0L);

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
	
	protected int calculateSegmentedStringSize(char[] chars, int offset, int length) {
		int valueArraySize = charArrayIndexStartOffset + (charArrayIndexScale * length);
		int segmentedStringSize = stringSize + valueArraySize + 2 * JvmUtil.OBJECT_ADDRESS_SENSIVITY; // Extra memory for possible aligning
		int segmentSizeMod = segmentedStringSize % STRING_SEGMENT_SIZE;
		if (segmentSizeMod != 0) {
			segmentedStringSize += (STRING_SEGMENT_SIZE - segmentSizeMod);
		}
		return segmentedStringSize;
	}
	
	protected int calculateSegmentedStringSize(char[] chars) {
		return calculateSegmentedStringSize(chars, 0, chars.length);
	}
	
	protected int calculateSegmentedStringSize(String str) {
		char[] valueArray = (char[]) directMemoryService.getObject(str, valueArrayOffsetInString);
		return calculateSegmentedStringSize(valueArray);
	}
	
	protected int calculateSegmentedStringSize(long strAddress) {
		char[] valueArray = 
				(char[]) 
					directMemoryService.getObject(
						JvmUtil.toNativeAddress(	
								directMemoryService.getAddress(
										strAddress + valueArrayOffsetInString)));
		int valueArraySize = charArrayIndexStartOffset + (charArrayIndexScale * valueArray.length);
		int segmentedStringSize = stringSize + valueArraySize + 2 * JvmUtil.OBJECT_ADDRESS_SENSIVITY; // Extra memory for possible aligning
		int segmentSizeMod = segmentedStringSize % STRING_SEGMENT_SIZE;
		if (segmentSizeMod != 0) {
			segmentedStringSize += (STRING_SEGMENT_SIZE - segmentSizeMod);
		}
		return segmentedStringSize;
	}
	
	protected int calculateStringSegmentCount(char[] chars, int offset, int length) {
		return calculateSegmentedStringSize(chars, offset, length) / STRING_SEGMENT_SIZE;
	}
	
	protected int calculateStringSegmentCount(char[] chars) {
		return calculateStringSegmentCount(chars, 0, chars.length);
	}
	
	protected int calculateStringSegmentCount(String str) {
		return calculateSegmentedStringSize(str) / STRING_SEGMENT_SIZE;
	}
	
	protected int calculateStringSegmentCount(long strAddress) {
		return calculateSegmentedStringSize(strAddress) / STRING_SEGMENT_SIZE;
	}
	
	protected byte getInUseFromStringSegment(long strSegment, int segmentCount) {
		if (strSegment + segmentCount >= totalSegmentCount) {
			return SEGMENT_BLOCK_IS_NOT_AVAILABLE;
		}
		for (int i = 0; i < segmentCount; i++) {
			byte segmentIndexValue = directMemoryService.getByteVolatile(inUseSegmentAddress + strSegment);
			if (segmentIndexValue == SEGMENT_IS_IN_USE) {
				return SEGMENT_BLOCK_IS_NOT_AVAILABLE;
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
			directMemoryService.putByteVolatile(inUseSegmentAddress + strSegment + i, 
					set ? SEGMENT_IS_IN_USE : SEGMENT_IS_AVAILABLE);
		}	
		if (full && !set) {
			currentSegmentIndex = strSegment - 1;
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
		if (full || currentSegmentIndex == INDEX_NOT_AVAILABLE) {
			return false;
		}
		
		currentSegmentIndex++;
		if (currentSegmentIndex >= totalSegmentCount) {
			currentSegmentIndex = 0;
		}
		
		byte segmentIndexInUse = getInUseFromStringSegment(currentSegmentIndex, segmentCount);
		int checkedSegmentCount = 0;
		while (checkedSegmentCount < totalSegmentCount) {
			if (segmentIndexInUse == SEGMENT_BLOCK_IS_AVAILABLE) {
				break;
			}
			currentSegmentIndex++;
			if (currentSegmentIndex >= totalSegmentCount) {
				currentSegmentIndex = 0;
			}
			segmentIndexInUse = getInUseFromStringSegment(currentSegmentIndex, segmentCount);
			checkedSegmentCount++;
		}
		
		if (segmentIndexInUse != SEGMENT_BLOCK_IS_AVAILABLE) {
			currentSegmentIndex = INDEX_NOT_AVAILABLE;
			full = true;
			return false;
		}	
		
		if (currentSegmentIndex < 0 || currentSegmentIndex >= totalSegmentCount) {
			logger.error("Invalid index for available string: " + currentSegmentIndex);
			return false;
		}
		
		currentAddress = stringsStartAddress + (currentSegmentIndex * STRING_SEGMENT_SIZE);
		currentSegmentIndex += (segmentCount - 1);
		
		return true;
	}
	
	protected synchronized String takeString(String str) {
		long objAddress = directMemoryService.addressOf(str);
		directMemoryService.putLong(objAddress, sampleHeader);
		int segmentCount = calculateStringSegmentCount(str);
		allocateStringFromStringAddress(objAddress, segmentCount);
		usedSegmentCount += segmentCount;
		return str;
	}
	
	protected synchronized String takeString(long strAddress) {
		String str = (String) directMemoryService.getObject(strAddress);
		directMemoryService.putLong(strAddress, sampleHeader);
		int segmentCount = calculateStringSegmentCount(strAddress);
		allocateStringFromStringAddress(strAddress, segmentCount);
		usedSegmentCount += segmentCount;
		return str;
	}
	
	protected synchronized long takeStringAsAddress(long strAddress) {
		directMemoryService.putLong(strAddress, sampleHeader);
		int segmentCount = calculateStringSegmentCount(strAddress);
		allocateStringFromStringAddress(strAddress, segmentCount);
		usedSegmentCount += segmentCount;
		return strAddress;
	}
	
	protected synchronized boolean releaseString(String str) {
		return releaseString(directMemoryService.addressOf(str));
	}
	
	protected synchronized boolean releaseString(long strAddress) {
		if (!isIn(strAddress)) {
			return false;
		}
//		if (getInUseFromStringAddress(strAddress, 1) == SEGMENT_BLOCK_IS_AVAILABLE) {
//			return false;
//		}
		// Reset free object
		int stringSegmentedSize = calculateSegmentedStringSize(strAddress);
		directMemoryService.setMemory(strAddress + JvmUtil.getHeaderSize(), 
									  stringSegmentedSize - JvmUtil.getHeaderSize(), 
									  (byte) 0x00);
		int segmentCount = stringSegmentedSize / STRING_SEGMENT_SIZE;
		freeStringFromStringAddress(strAddress, segmentCount);
		usedSegmentCount -= segmentCount;
		return true;
	}
	
	protected long allocateStringFromOffHeap(char[] chars, int offset, int length) {
		int valueArraySize = charArrayIndexStartOffset + (charArrayIndexScale * length);
		int strSize = stringSize + valueArraySize + 2 * JvmUtil.OBJECT_ADDRESS_SENSIVITY; // Extra memory for possible aligning
		
		long addressMod1 = currentAddress % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
		if (addressMod1 != 0) {
			currentAddress += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - addressMod1);
		}
		
		if (currentAddress + strSize > allocationEndAddress) {
			return JvmUtil.NULL;
		}
		
		// Copy string object content to allocated area
		directMemoryService.copyMemory(sampleStr, 0, null, currentAddress, stringSize);
		
		long valueAddress = currentAddress + stringSize;
		long addressMod2 = valueAddress % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
		if (addressMod2 != 0) {
			valueAddress += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - addressMod2);
		}

		// Copy char array header to allocated char array
		directMemoryService.copyMemory(
				chars, 0L,
				null, valueAddress, 
				charArrayIndexStartOffset);
		// Copy chars to allocated char array
		directMemoryService.copyMemory(
				chars, charArrayIndexStartOffset + (charArrayIndexScale * offset),
				null, valueAddress + charArrayIndexStartOffset, 
				(charArrayIndexScale * length));
		// Set array length
		JvmUtil.setArrayLength(valueAddress, char.class, length);

		// Now, value array in allocated string points to allocated char array
		directMemoryService.putAddress(
				currentAddress + valueArrayOffsetInString, 
				JvmUtil.toJvmAddress(valueAddress));
		
		return takeStringAsAddress(currentAddress);
	}
	
	protected long allocateStringFromOffHeap(String str) {
		char[] valueArray = (char[]) directMemoryService.getObject(str, valueArrayOffsetInString);
		int valueArraySize = charArrayIndexStartOffset + (charArrayIndexScale * valueArray.length);
		int strSize = stringSize + valueArraySize + 2 * JvmUtil.OBJECT_ADDRESS_SENSIVITY; // Extra memory for possible aligning
				
		long addressMod1 = currentAddress % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
		if (addressMod1 != 0) {
			currentAddress += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - addressMod1);
		}
				
		if (currentAddress + strSize > allocationEndAddress) {
			return JvmUtil.NULL;
		}
				
		// Copy string object content to allocated area
		directMemoryService.copyMemory(str, 0, null, currentAddress, stringSize);
				
		long valueAddress = currentAddress + stringSize;
		long addressMod2 = valueAddress % JvmUtil.OBJECT_ADDRESS_SENSIVITY;
		if (addressMod2 != 0) {
			valueAddress += (JvmUtil.OBJECT_ADDRESS_SENSIVITY - addressMod2);
		}

		// Copy value array in allocated string to allocated char array
		directMemoryService.copyMemory(
				valueArray, 0L,
				null, valueAddress, 
				valueArraySize);
				
		// Now, value array in string points to allocated char array
		directMemoryService.putAddress(
				currentAddress + valueArrayOffsetInString, 
				JvmUtil.toJvmAddress(valueAddress));
				
		return takeStringAsAddress(currentAddress);
	}
	
	@Override
	public boolean isFull() {
		return full;
	}
	
	@Override
	public boolean isEmpty() {
		return usedSegmentCount == 0;
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
	public synchronized String get(char[] chars) {
		if (chars == null) {
			return null;
		}
		return getInternal(chars, 0, chars.length);
	}
	
	@Override
	public synchronized String get(char[] chars, int offset, int length) {
		if (chars == null) {
			return null;
		}
		return getInternal(chars, offset, length);
	}
	
	protected String getInternal(char[] chars, int offset, int length) {
		checkAvailability();
		int requiredSegmentCount = calculateStringSegmentCount(chars, offset, length);
		if (!nextAvailable(requiredSegmentCount)) {
			return null;
		}
		long allocatedStrAddress = allocateStringFromOffHeap(chars, offset, length);
		if (allocatedStrAddress == JvmUtil.NULL) {
			return null;
		}
		else {
			return directMemoryService.getObject(allocatedStrAddress);
		}
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
		directMemoryService.freeMemory(inUseSegmentAddress);
		directMemoryService.freeMemory(allocationStartAddress);
		usedSegmentCount = 0;
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
