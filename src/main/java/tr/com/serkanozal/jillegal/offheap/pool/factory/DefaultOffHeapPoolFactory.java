/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.factory;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ArrayOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.BaseOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ExtendableObjectOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.SequentialObjectOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.ComplexTypeArrayOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.EagerReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.ExtendableObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.LazyReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.PrimitiveTypeArrayOffHeapPool;

public class DefaultOffHeapPoolFactory implements OffHeapPoolFactory {

	private DirectMemoryService directMemoryService = DirectMemoryServiceFactory.getDirectMemoryService();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T, O extends OffHeapPool<T, ?>> O  createOffHeapPool(OffHeapPoolCreateParameter<T> parameter) {
		Class<?> elementType = parameter.getElementType();
		if (elementType.equals(String.class) || elementType.equals(String[].class)) {
			throw new IllegalArgumentException("\"String\" and \"String[]\" types are not supported yet !");
		}
		
		assignOffHeapMemoryServiceIfNeeded(parameter);
		
		OffHeapPoolType offHeapPoolType = parameter.getOffHeapPoolType();
		switch (offHeapPoolType) {
			case SEQUENTIAL_OBJECT_POOL:
				SequentialObjectOffHeapPoolCreateParameter<T> seqObjPoolParameter = (SequentialObjectOffHeapPoolCreateParameter<T>)parameter; 
				switch (seqObjPoolParameter.getReferenceType()) {
					case LAZY_REFERENCED:
						return (O) new LazyReferencedObjectOffHeapPool<T>((SequentialObjectOffHeapPoolCreateParameter<T>)parameter);
					case EAGER_REFERENCED:
						return (O) new EagerReferencedObjectOffHeapPool<T>((SequentialObjectOffHeapPoolCreateParameter<T>)parameter);
					default:
						return (O) new LazyReferencedObjectOffHeapPool<T>((SequentialObjectOffHeapPoolCreateParameter<T>)parameter);
				}
			case ARRAY_POOL:
				ArrayOffHeapPoolCreateParameter<T> arrayPoolParameter = (ArrayOffHeapPoolCreateParameter<T>)parameter; 
				if (arrayPoolParameter.isUsePrimitiveTypes()) {
					return (O) new PrimitiveTypeArrayOffHeapPool<T, T[]>(arrayPoolParameter);
				}
				else {
					return (O) new ComplexTypeArrayOffHeapPool<T>(arrayPoolParameter);
				}
			case EXTENDABLE_OBJECT_POOL:
				return (O) new ExtendableObjectOffHeapPool<T>((ExtendableObjectOffHeapPoolCreateParameter<T>)parameter);
			default:
				throw new IllegalArgumentException("Unsupported off heap pool type: " + offHeapPoolType);
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
