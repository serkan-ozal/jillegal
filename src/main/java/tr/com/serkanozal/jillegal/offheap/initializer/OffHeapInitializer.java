/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.initializer;

import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;

public class OffHeapInitializer {

	private OffHeapInitializer() {
		
	}
	
	public static void init() {
		DirectMemoryServiceFactory.init();
	}
	
}
