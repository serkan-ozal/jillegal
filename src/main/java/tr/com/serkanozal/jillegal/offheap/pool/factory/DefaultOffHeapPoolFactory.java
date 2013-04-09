/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.factory;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.BaseOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.SequentialObjectPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.pool.EagerReferencedObjectPool;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.LazyReferencedObjectPool;

public class DefaultOffHeapPoolFactory implements OffHeapPoolFactory {

	private DirectMemoryService directMemoryService = DirectMemoryServiceFactory.getDirectMemoryService();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T, O extends OffHeapPool<T, ?>> O  createOffHeapPool(OffHeapPoolCreateParameter<T> parameter) {
		assignOffHeapMemoryServiceIfNeeded(parameter);
		
		switch (parameter.getPoolType()) {
			case SEQUENTIAL_OBJECT_POOL:
				SequentialObjectPoolCreateParameter<T> seqObjPoolParameter = (SequentialObjectPoolCreateParameter<T>)parameter; 
				switch (seqObjPoolParameter.getReferenceType()) {
					case LAZY_REFERENCED:
						return (O) new LazyReferencedObjectPool<T>((SequentialObjectPoolCreateParameter<T>)parameter);
					case EAGER_REFERENCED:
						return (O) new EagerReferencedObjectPool<T>((SequentialObjectPoolCreateParameter<T>)parameter);
					default:
						return (O) new LazyReferencedObjectPool<T>((SequentialObjectPoolCreateParameter<T>)parameter);
				}
			default:
				return null;
		}
	}
	
	protected <T> void assignOffHeapMemoryServiceIfNeeded(OffHeapPoolCreateParameter<T> parameter) {
		if (parameter instanceof BaseOffHeapPoolCreateParameter) {
			BaseOffHeapPoolCreateParameter<T> baseParameter = (BaseOffHeapPoolCreateParameter<T>)parameter;
			if (baseParameter.getDirectMemoryService() == null) {
				baseParameter.setDirectMemoryService(directMemoryService);
			}
		}
	}

}
