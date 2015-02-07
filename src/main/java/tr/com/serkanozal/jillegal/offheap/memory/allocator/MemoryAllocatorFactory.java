/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.memory.allocator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tr.com.serkanozal.jillegal.util.JvmUtil;

public class MemoryAllocatorFactory {

	private static final Map<MemoryAllocatorType, MemoryAllocator> MEMORY_ALLOCATOR_MAP =
			new ConcurrentHashMap<MemoryAllocatorType, MemoryAllocator>();
	private static final MemoryAllocator DEFAULT_MEMORY_ALLOCATOR;
	
	static {
		MEMORY_ALLOCATOR_MAP.put(MemoryAllocatorType.STANDARD, new StandardMemoryAllocator());
		MEMORY_ALLOCATOR_MAP.put(MemoryAllocatorType.BATCH, new BatchMemoryAllocator());
		if (JvmUtil.getAddressSize() == JvmUtil.SIZE_32_BIT) {
			DEFAULT_MEMORY_ALLOCATOR = MEMORY_ALLOCATOR_MAP.get(MemoryAllocatorType.STANDARD);
		} 
		else {
			if (Boolean.getBoolean("jillegal.offheap.memory.useBatchMemoryAllocatorOn64BitJVM")) {
				DEFAULT_MEMORY_ALLOCATOR = MEMORY_ALLOCATOR_MAP.get(MemoryAllocatorType.BATCH);
			}
			else {
				DEFAULT_MEMORY_ALLOCATOR = MEMORY_ALLOCATOR_MAP.get(MemoryAllocatorType.STANDARD);
			}	
		}
	}

	private MemoryAllocatorFactory() {
		
	}
	
	public static MemoryAllocator getMemoryAllocator(MemoryAllocatorType memoryAllocatorType) {
		return MEMORY_ALLOCATOR_MAP.get(memoryAllocatorType);
	}
	
	public static MemoryAllocator getDefaultMemoryAllocator() {
		return DEFAULT_MEMORY_ALLOCATOR;
	}
	
}
