/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.service;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.factory.OffHeapPoolFactory;

public interface OffHeapService {

	OffHeapPoolFactory getDefaultOffHeapPoolFactory();
	void setOffHeapPoolFactory(OffHeapPoolFactory offHeapPoolFactory);
	
	<P extends OffHeapPoolCreateParameter<?>> OffHeapPoolFactory getOffHeapPoolFactory(Class<P> clazz);
	<P extends OffHeapPoolCreateParameter<?>> void setOffHeapPoolFactory(OffHeapPoolFactory offHeapPoolFactory, Class<P> clazz);
	
	<T, O extends OffHeapPool<T, ?>> O createOffHeapPool(OffHeapPoolCreateParameter<T> parameter);
	
}
