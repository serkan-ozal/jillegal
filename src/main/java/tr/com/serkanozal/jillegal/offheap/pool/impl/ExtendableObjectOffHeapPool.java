/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import java.util.ArrayList;
import java.util.List;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ExtendableObjectOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;

public class ExtendableObjectOffHeapPool<T> extends BaseOffHeapPool<T, ExtendableObjectOffHeapPoolCreateParameter<T>> 
		implements ObjectOffHeapPool<T, ExtendableObjectOffHeapPoolCreateParameter<T>> {

	protected DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> rootForkableOffHeapPool;
	protected List<DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>>> previousForkableOffHeapPoolList = 
					new ArrayList<DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>>>();
	protected DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> currentForkableOffHeapPool;
			
	public ExtendableObjectOffHeapPool(ExtendableObjectOffHeapPoolCreateParameter<T> parameter) {
		this(parameter.getElementType(), parameter.getForkableObjectOffHeapPool(), parameter.getDirectMemoryService());
	}
	
	public ExtendableObjectOffHeapPool(Class<T> clazz, 
			DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool, 
			DirectMemoryService directMemoryService) {
		super(clazz, directMemoryService);
		init(forkableOffHeapPool);
	}
	
	protected synchronized void init() {
		currentForkableOffHeapPool = rootForkableOffHeapPool;
	}

	@Override
	public synchronized T get() {
		T obj = currentForkableOffHeapPool.get();
		if (obj == null) {
			extend();
			return currentForkableOffHeapPool.get();
		}
		else {
			return obj;
		}
	}
	
	@Override
	public void reset() {
		for (DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool : previousForkableOffHeapPoolList) {
			if (forkableOffHeapPool != rootForkableOffHeapPool) {
				forkableOffHeapPool.free();
			}	
		}
		if (currentForkableOffHeapPool != null && currentForkableOffHeapPool != rootForkableOffHeapPool) {
			currentForkableOffHeapPool.free();
		}
		init();
	}
	
	@Override
	public void free() {
		for (DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool : previousForkableOffHeapPoolList) {
			forkableOffHeapPool.free();
		}
		if (currentForkableOffHeapPool != null) {
			currentForkableOffHeapPool.free();
		}
	}

	@Override
	public void init(ExtendableObjectOffHeapPoolCreateParameter<T> parameter) {
		init(parameter.getForkableObjectOffHeapPool());
	}
	
	protected void init(DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool) {
		rootForkableOffHeapPool = forkableOffHeapPool;
		init();
	}
	
	protected void extend() {
		DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> newForkableOffHeapPool = 
				currentForkableOffHeapPool.fork();
		previousForkableOffHeapPoolList.add(currentForkableOffHeapPool);
		currentForkableOffHeapPool = 
				(DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>>) newForkableOffHeapPool;
	}
	
}
