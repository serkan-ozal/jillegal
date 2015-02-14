/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.memory.allocator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.util.JvmUtil;

@SuppressWarnings( { "restriction" } )
public class StandardMemoryAllocator implements MemoryAllocator {

	private static final Unsafe UNSAFE = JvmUtil.getUnsafe();

	private final Map<Long, Long> memoryMappings = new ConcurrentHashMap<Long, Long>();
	private final AtomicLong allocatedMemory = new AtomicLong();
	
	@Override
	public long allocateMemory(long size) {
		long address = UNSAFE.allocateMemory(size);
		if (address > 0) {
			memoryMappings.put(address, size);
//			allocatedMemory.addAndGet(size);
		}
		return address;
	}
	
	@Override
	public void freeMemory(long address) {
		Long size = memoryMappings.remove(address);
		if (size != null) {
//			allocatedMemory.addAndGet(-size);
		}
		UNSAFE.freeMemory(address);
	}
	
	@Override
	public long totalMemory() {
		return allocatedMemory.get();
	}
	
	@Override
	public long usedMemory() {
		return allocatedMemory.get();
	}
	
}
