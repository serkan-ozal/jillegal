/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.factory.DefaultOffHeapPoolFactory;
import tr.com.serkanozal.jillegal.offheap.pool.factory.OffHeapPoolFactory;

public class OffHeapServiceImpl implements OffHeapService {

	private final Logger logger = Logger.getLogger(getClass());
	
	private OffHeapPoolFactory defaultOffHeapPoolFactory = new DefaultOffHeapPoolFactory();
	private Map<Class<? extends OffHeapPoolCreateParameter<?>>, OffHeapPoolFactory> offHeapPoolFactoryMap = 
			new HashMap<Class<? extends OffHeapPoolCreateParameter<?>>, OffHeapPoolFactory>();
	
	public OffHeapServiceImpl() {
		init();
	}
	
	protected void init() {
		
	}

	protected <P extends OffHeapPoolCreateParameter<?>> OffHeapPoolFactory findOffHeapPoolFactory(Class<P> clazz) {
		OffHeapPoolFactory offHeapPoolFactory = offHeapPoolFactoryMap.get(clazz);
		if (offHeapPoolFactory != null) {
			return offHeapPoolFactory;
		}
		else {
			return defaultOffHeapPoolFactory;
		}
	}
	
	@Override
	public OffHeapPoolFactory getDefaultOffHeapPoolFactory() {
		return defaultOffHeapPoolFactory;
	}

	@Override
	public void setOffHeapPoolFactory(OffHeapPoolFactory offHeapPoolFactory) {
		defaultOffHeapPoolFactory = offHeapPoolFactory;
	}

	@Override
	public <P extends OffHeapPoolCreateParameter<?>> OffHeapPoolFactory getOffHeapPoolFactory(Class<P> clazz) {
		return offHeapPoolFactoryMap.get(clazz);
	}

	@Override
	public <P extends OffHeapPoolCreateParameter<?>> void setOffHeapPoolFactory(OffHeapPoolFactory offHeapPoolFactory, Class<P> clazz) {
		offHeapPoolFactoryMap.put(clazz, offHeapPoolFactory);
	}
	
	@Override
	public <T, O extends OffHeapPool<T, ?>> O createOffHeapPool(OffHeapPoolCreateParameter<T> parameter) {
		OffHeapPoolFactory offHeapPoolFactory = findOffHeapPoolFactory(parameter.getClass());
		if (offHeapPoolFactory != null) {
			return offHeapPoolFactory.createOffHeapPool(parameter);
		}
		else {
			logger.warn("OffHeapPool couldn't be found for class " + parameter.getClass().getName());
			return null;
		}
	}
	
}
