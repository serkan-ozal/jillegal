/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;

public interface ExplicitStringOffHeapPool<P extends OffHeapPoolCreateParameter<String>> 
		extends OffHeapPool<String, P>, 
				FreeableOffHeapPool<String, P> {

	String get(String str);
	long getAsAddress(String str);
	
}
