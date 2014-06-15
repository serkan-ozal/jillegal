/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import java.util.ArrayList;
import java.util.List;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ExtendableArrayOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableArrayOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ArrayOffHeapPool;

public class ExtendableArrayOffHeapPool<T, A> extends BaseOffHeapPool<T, ExtendableArrayOffHeapPoolCreateParameter<T, A>> 
		implements ArrayOffHeapPool<T, A, ExtendableArrayOffHeapPoolCreateParameter<T, A>> {

	protected DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> rootForkableOffHeapPool;
	protected List<DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>>> previousForkableOffHeapPoolList = 
					new ArrayList<DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>>>();
	protected DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> currentForkableOffHeapPool;
			
	public ExtendableArrayOffHeapPool(ExtendableArrayOffHeapPoolCreateParameter<T, A> parameter) {
		this(parameter.getElementType(), parameter.getForkableArrayOffHeapPool(), parameter.getDirectMemoryService());
	}
	
	public ExtendableArrayOffHeapPool(Class<T> clazz, 
			DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool, 
			DirectMemoryService directMemoryService) {
		super(clazz, directMemoryService);
		init(forkableOffHeapPool);
	}
	
	protected synchronized void init() {
		currentForkableOffHeapPool = rootForkableOffHeapPool;
	}

	@Override
	public A getArray() {
		return currentForkableOffHeapPool.getArray();
	}
	
	@Override
	public int getLength() {
		return Integer.MAX_VALUE;
	}
	
	@Override
	public T getAt(int index) {
		// TODO Expand if index is out of limits
		return currentForkableOffHeapPool.getAt(index);
	}

	@Override
	public void setAt(T element, int index) {
		// TODO Expand if index is out of limits
		currentForkableOffHeapPool.setAt(element, index);
	}
	
	@Override
	public void reset() {
		for (DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool : previousForkableOffHeapPoolList) {
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
		for (DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool : previousForkableOffHeapPoolList) {
			forkableOffHeapPool.free();
		}
		if (currentForkableOffHeapPool != null) {
			currentForkableOffHeapPool.free();
		}
	}

	@Override
	public void init(ExtendableArrayOffHeapPoolCreateParameter<T, A> parameter) {
		init(parameter.getForkableArrayOffHeapPool());
	}
	
	protected void init(DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool) {
		rootForkableOffHeapPool = forkableOffHeapPool;
		init();
	}
	
	protected void extend() {
		DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> newForkableOffHeapPool = 
				currentForkableOffHeapPool.fork();
		previousForkableOffHeapPoolList.add(currentForkableOffHeapPool);
		currentForkableOffHeapPool = 
				(DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>>) newForkableOffHeapPool;
	}

}
