/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.pool.factory;

import tr.com.serkanozal.jillegal.domain.model.pool.BaseOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.domain.model.pool.SequentialObjectPoolCreateParameter;
import tr.com.serkanozal.jillegal.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.pool.SequentialObjectPool;
import tr.com.serkanozal.jillegal.service.OffHeapMemoryService;
import tr.com.serkanozal.jillegal.service.UnsafeBasedOffHeapMemoryServiceImpl;

public class DefaultOffHeapPoolFactory implements OffHeapPoolFactory {

	private OffHeapMemoryService offHeapMemoryService = new UnsafeBasedOffHeapMemoryServiceImpl();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T, O extends OffHeapPool<T, ?>> O  createOffHeapPool(OffHeapPoolCreateParameter<T> parameter) {
		assignOffHeapMemoryServiceIfNeeded(parameter);
		
		switch (parameter.getPoolType()) {
			case SEQUENTIAL_OBJECT_POOL:
				return (O) new SequentialObjectPool<T>((SequentialObjectPoolCreateParameter<T>)parameter);
				
			default:
				return null;
		}
	}
	
	protected <T> void assignOffHeapMemoryServiceIfNeeded(OffHeapPoolCreateParameter<T> parameter) {
		if (parameter instanceof BaseOffHeapPoolCreateParameter) {
			BaseOffHeapPoolCreateParameter<T> baseParameter = (BaseOffHeapPoolCreateParameter<T>)parameter;
			if (baseParameter.getOffHeapMemoryService() == null) {
				baseParameter.setOffHeapMemoryService(offHeapMemoryService);
			}
		}
	}

}
