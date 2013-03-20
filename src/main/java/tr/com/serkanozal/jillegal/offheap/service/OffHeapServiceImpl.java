/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.service;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.factory.DefaultOffHeapPoolFactory;
import tr.com.serkanozal.jillegal.offheap.pool.factory.OffHeapPoolFactory;

public class OffHeapServiceImpl implements OffHeapService {

	private OffHeapPoolFactory defaultOffHeapPoolFactory = new DefaultOffHeapPoolFactory();

	@Override
	public <T, O extends OffHeapPool<T, ?>> O createOffHeapPool(OffHeapPoolCreateParameter<T> parameter) {
		OffHeapPoolFactory offHeapPoolFactory = getPoolFactory(parameter);
		if (offHeapPoolFactory == null) {
			offHeapPoolFactory = defaultOffHeapPoolFactory;
		}
		return offHeapPoolFactory.createOffHeapPool(parameter);
	}
	
	protected <T> OffHeapPoolFactory getPoolFactory(OffHeapPoolCreateParameter<T> parameter) {
		// TODO Determine OffHeapPoolFactory by using parameter
		return defaultOffHeapPoolFactory;
	}
	
}
