/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap;

import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;

public interface OffHeapAwareObject {

	void onGet(OffHeapService offHeapService, DirectMemoryService directMemoryService);
	void onFree(OffHeapService offHeapService, DirectMemoryService directMemoryService);
	
}
