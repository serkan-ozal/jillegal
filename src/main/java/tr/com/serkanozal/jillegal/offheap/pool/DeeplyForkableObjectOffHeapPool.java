/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;

public interface DeeplyForkableObjectOffHeapPool<T, P extends OffHeapPoolCreateParameter<T>>
		extends DeeplyForkableOffHeapPool<T, P>, ObjectOffHeapPool<T, P> {

	DeeplyForkableObjectOffHeapPool<T, P> fork();
	
}
