/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.memory.allocator;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.util.JvmUtil;

@SuppressWarnings( { "restriction" } )
public class StandardMemoryAllocator implements MemoryAllocator {

	private static final Unsafe UNSAFE = JvmUtil.getUnsafe();
	
	@Override
	public long allocateMemory(long size) {
		return UNSAFE.allocateMemory(size);
	}
	
	@Override
	public void freeMemory(long address) {
		UNSAFE.freeMemory(address);
	}
	
}
