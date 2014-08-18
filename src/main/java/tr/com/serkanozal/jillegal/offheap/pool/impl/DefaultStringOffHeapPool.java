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
	protected String sampleStr;
	protected long sampleStrAddress;
	protected char[] sampleCharArray;
	
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
	
	@SuppressWarnings({ "deprecation" })
	@Override
	protected void init() {
		try {
			super.init();
			
			charArrayIndexScale = JvmUtil.arrayIndexScale(char.class);
			charArrayIndexStartOffset = JvmUtil.arrayBaseOffset(char.class);
			valueArrayOffsetInString = JvmUtil.getUnsafe().fieldOffset(String.class.getDeclaredField("value"));
			stringSize = (int) JvmUtil.sizeOf(String.class);
			int estimatedStringSize = (int) (stringSize + JvmUtil.sizeOfArray(char.class, estimatedStringLength));
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
			sampleStr = new String();
			sampleStrAddress = JvmUtil.addressOf(sampleStr);
			sampleCharArray = new char[0];
		}
		catch (Throwable t) {
			logger.error("Error occured while initializing \"StringOffHeapPool\"", t);
			throw new IllegalStateException(t);
		}
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
			sampleStr = new String();
			sampleStrAddress = JvmUtil.addressOf(sampleStr);
			sampleCharArray = new char[0];
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
	
	@Override
	public synchronized String get(String str) {
		checkAvailability();
		long allocatedStrAddress = allocateStringFromOffHeap(str);
		if (allocatedStrAddress == 0) {
			return null;
		}
		else {
			return directMemoryService.getObject(allocatedStrAddress);
		}
	}
	
	@Override
	public synchronized long getAsAddress(String str) {
		checkAvailability();
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
	public boolean free(String str) {
		checkAvailability();
		if (str == null) {
			return false;
		}
		return freeFromAddress(directMemoryService.addressOf(str));
	}
	
	@Override
	public boolean freeFromAddress(long strAddress) {
		checkAvailability();
		if (!isIn(strAddress)) {
			return false;
		}
		return false;
	}
	
	protected long allocateStringFromOffHeap(String str) {
		long addressOfStr = JvmUtil.addressOf(str);
		char[] valueArray = (char[]) directMemoryService.getObject(str, valueArrayOffsetInString);
		int valueArraySize = charArrayIndexStartOffset + (charArrayIndexScale * valueArray.length);
		int strSize = stringSize + valueArraySize + JvmUtil.getAddressSize(); // Extra memory for possible aligning
		
		long addressMod1 = currentAddress % JvmUtil.getAddressSize();
		if (addressMod1 != 0) {
			currentAddress += (JvmUtil.getAddressSize() - addressMod1);
		}
		
		if (currentAddress + strSize > allocationEndAddress) {
			return 0;
		}
		
		// Copy string object content to allocated area
		directMemoryService.copyMemory(addressOfStr, currentAddress, strSize);
		
		long valueAddress = currentAddress + stringSize;
		long addressMod2 = valueAddress % JvmUtil.getAddressSize();
		if (addressMod2 != 0) {
			valueAddress += (JvmUtil.getAddressSize() - addressMod2);
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
		
		long allocatedStrAddress = currentAddress;

		currentAddress += strSize;
		
		return allocatedStrAddress;
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
