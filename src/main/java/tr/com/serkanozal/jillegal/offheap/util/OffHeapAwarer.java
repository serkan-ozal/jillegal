/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.util;

public class OffHeapAwarer {

	private OffHeapAwarer() {
        
    }
	
	public static <T> void doOffHeapAware(T obj) {
		OffHeapUtil.injectOffHeapFields(obj);
	}
    
}
