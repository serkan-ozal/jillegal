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
import tr.com.serkanozal.jillegal.offheap.pool.ContentAwareOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;

public class ExtendableObjectOffHeapPool<T> extends BaseOffHeapPool<T, ExtendableObjectOffHeapPoolCreateParameter<T>> 
		implements 	ObjectOffHeapPool<T, ExtendableObjectOffHeapPoolCreateParameter<T>>,
					ContentAwareOffHeapPool<T, ExtendableObjectOffHeapPoolCreateParameter<T>> {

	protected DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> rootForkableOffHeapPool;
	protected List<DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>>> forkableOffHeapPoolList = 
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
	
	@Override
	protected void init() {
		super.init();
		
		currentForkableOffHeapPool = rootForkableOffHeapPool;
	}

	@Override
	public synchronized T get() {
		checkAvailability();
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
	public synchronized long getAsAddress() {
		checkAvailability();
		return currentForkableOffHeapPool.getAsAddress();
	}
	
	@Override
	public boolean isMine(T element) {
		checkAvailability();
		if (element == null) {
			return false;
		}
		else {
			return isMine(directMemoryService.addressOf(element));
		}	
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isMine(long address) {
		checkAvailability();
		if (currentForkableOffHeapPool instanceof ContentAwareOffHeapPool) {
			if (((ContentAwareOffHeapPool)currentForkableOffHeapPool).isMine(address)) {
				return true;
			}
		}
		for (DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool : forkableOffHeapPoolList) {
			if (forkableOffHeapPool != currentForkableOffHeapPool && forkableOffHeapPool instanceof ContentAwareOffHeapPool) {
				if (((ContentAwareOffHeapPool)forkableOffHeapPool).isMine(address)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public synchronized boolean free(T obj) {
		checkAvailability();
		if (obj != null) {
			return isMine(directMemoryService.addressOf(obj));
		}	
		else {
			return false;
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public synchronized boolean freeFromAddress(long objAddress) {
		checkAvailability();
		if (currentForkableOffHeapPool instanceof ContentAwareOffHeapPool) {
			if (((ContentAwareOffHeapPool)currentForkableOffHeapPool).isMine(objAddress)) {
				return currentForkableOffHeapPool.freeFromAddress(objAddress);
			}
		}
		for (DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool : forkableOffHeapPoolList) {
			if (forkableOffHeapPool != currentForkableOffHeapPool && forkableOffHeapPool instanceof ContentAwareOffHeapPool) {
				if (((ContentAwareOffHeapPool)forkableOffHeapPool).isMine(objAddress)) {
					return currentForkableOffHeapPool.freeFromAddress(objAddress);
				}
			}
		}
		return false;
	}
	
	@Override
	public synchronized void reset() {
		for (DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool : forkableOffHeapPoolList) {
			if (forkableOffHeapPool != rootForkableOffHeapPool) {
				forkableOffHeapPool.free();
			}	
		}
		if (currentForkableOffHeapPool != null && currentForkableOffHeapPool != rootForkableOffHeapPool) {
			currentForkableOffHeapPool.free();
		}
		init();
		makeAvaiable();
	}
	
	@Override
	public synchronized void free() {
		checkAvailability();
		for (DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool : forkableOffHeapPoolList) {
			forkableOffHeapPool.free();
		}
		if (currentForkableOffHeapPool != null) {
			currentForkableOffHeapPool.free();
		}
		makeUnavaiable();
	}

	@Override
	public synchronized void init(ExtendableObjectOffHeapPoolCreateParameter<T> parameter) {
		init(parameter.getForkableObjectOffHeapPool());
	}
	
	protected void init(DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool) {
		rootForkableOffHeapPool = forkableOffHeapPool;
		init();
		makeAvaiable();
	}
	
	protected void extend() {
		DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> newForkableOffHeapPool = 
				currentForkableOffHeapPool.fork();
		forkableOffHeapPoolList.add(currentForkableOffHeapPool);
		currentForkableOffHeapPool = 
				(DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>>) newForkableOffHeapPool;
	}
	
}
