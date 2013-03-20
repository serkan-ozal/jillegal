/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.pool.factory;

import tr.com.serkanozal.jillegal.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.pool.OffHeapPool;

public interface OffHeapPoolFactory {

	<T, O extends OffHeapPool<T, ?>> O createOffHeapPool(OffHeapPoolCreateParameter<T> parameter);
	
}
