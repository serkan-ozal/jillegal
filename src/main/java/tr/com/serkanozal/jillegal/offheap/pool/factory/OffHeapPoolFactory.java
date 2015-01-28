/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.factory;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.pool.ArrayOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;

public interface OffHeapPoolFactory {

	@SuppressWarnings("rawtypes")
	<T, O extends OffHeapPool> O createOffHeapPool(OffHeapPoolCreateParameter<T> parameter);
	@SuppressWarnings("rawtypes")
	<T, O extends ObjectOffHeapPool> O createObjectOffHeapPool(Class<T> objectType, int objectCount);
	@SuppressWarnings("rawtypes")
	<T, A, O extends ArrayOffHeapPool> O createArrayOffHeapPool(Class<A> arrayType, int arrayLength, 
			boolean initializeElements);
	
}
