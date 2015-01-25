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
import tr.com.serkanozal.jillegal.util.JvmUtil;

public class ExtendableObjectOffHeapPool<T> extends BaseOffHeapPool<T, ExtendableObjectOffHeapPoolCreateParameter<T>> 
		implements 	ObjectOffHeapPool<T, ExtendableObjectOffHeapPoolCreateParameter<T>>,
					ContentAwareOffHeapPool<T, ExtendableObjectOffHeapPoolCreateParameter<T>> {

	protected DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> rootForkableOffHeapPool;
	protected List<DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>>> forkableOffHeapPoolList = 
					new ArrayList<DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>>>();
	protected volatile DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> currentForkableOffHeapPool;
	protected volatile DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> lastUsedForkableOffHeapPoolToFree;
			
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
		lastUsedForkableOffHeapPoolToFree = null;
	}
	
	@Override
	public boolean isFull() {
		if (!currentForkableOffHeapPool.isFull()) {
			return false;
		}
		for (int i = 0; i < forkableOffHeapPoolList.size(); i++) {
			DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool = 
					forkableOffHeapPoolList.get(i);
			if (!forkableOffHeapPool.isFull()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean isEmpty() {
		for (int i = 0; i < forkableOffHeapPoolList.size(); i++) {
			DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool = 
					forkableOffHeapPoolList.get(i);
			if (!forkableOffHeapPool.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public synchronized T get() {
		checkAvailability();
		T obj = currentForkableOffHeapPool.get();
		if (obj == null) {
			for (int i = 0; i < forkableOffHeapPoolList.size(); i++) {
				DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool = 
						forkableOffHeapPoolList.get(i);
				if (!forkableOffHeapPool.isFull()) {
					obj = forkableOffHeapPool.get();
					if (obj != null) {
						return obj;
					}
				}
			}
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
		long address = currentForkableOffHeapPool.getAsAddress();
		if (address == JvmUtil.NULL) {
			for (int i = 0; i < forkableOffHeapPoolList.size(); i++) {
				DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool = 
						forkableOffHeapPoolList.get(i);
				if (!forkableOffHeapPool.isFull()) {
					address = forkableOffHeapPool.getAsAddress();
					if (address != JvmUtil.NULL) {
						return address;
					}
				}
			}
			extend();
			return currentForkableOffHeapPool.getAsAddress();
		}
		else {
			return address;
		}
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
		for (int i = 0; i < forkableOffHeapPoolList.size(); i++) {
			DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool = 
					forkableOffHeapPoolList.get(i);
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
		if (obj == null) {
			return false;
		}
		if (currentForkableOffHeapPool.free(obj)) {
			if (currentForkableOffHeapPool.isEmpty() && 
					currentForkableOffHeapPool != rootForkableOffHeapPool) {
				currentForkableOffHeapPool.free();
				forkableOffHeapPoolList.remove(currentForkableOffHeapPool);
				currentForkableOffHeapPool = rootForkableOffHeapPool;
			}
			lastUsedForkableOffHeapPoolToFree = currentForkableOffHeapPool;
			return true;
		}
		if (lastUsedForkableOffHeapPoolToFree != null) {
			if (lastUsedForkableOffHeapPoolToFree.free(obj)) {
				if (lastUsedForkableOffHeapPoolToFree.isEmpty() && 
						lastUsedForkableOffHeapPoolToFree != rootForkableOffHeapPool) {
					lastUsedForkableOffHeapPoolToFree.free();
					forkableOffHeapPoolList.remove(lastUsedForkableOffHeapPoolToFree);
					currentForkableOffHeapPool = rootForkableOffHeapPool;
					lastUsedForkableOffHeapPoolToFree = currentForkableOffHeapPool;
				}
				return true;
			}
		}
		for (int i = 0; i < forkableOffHeapPoolList.size(); i++) {
			DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool = 
					forkableOffHeapPoolList.get(i);
			if (forkableOffHeapPool != currentForkableOffHeapPool) {
				if (forkableOffHeapPool.free(obj)) {
					if (forkableOffHeapPool.isEmpty() && forkableOffHeapPool != rootForkableOffHeapPool) {
						forkableOffHeapPool.free();
						forkableOffHeapPoolList.remove(forkableOffHeapPool);
						currentForkableOffHeapPool = rootForkableOffHeapPool;
						lastUsedForkableOffHeapPoolToFree = currentForkableOffHeapPool;
					} 
					else {
						lastUsedForkableOffHeapPoolToFree = forkableOffHeapPool;
					}	
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public synchronized boolean freeFromAddress(long objAddress) {
		checkAvailability();
		if (currentForkableOffHeapPool.freeFromAddress(objAddress)) {
			if (currentForkableOffHeapPool.isEmpty() && 
					currentForkableOffHeapPool != rootForkableOffHeapPool) {
				currentForkableOffHeapPool.free();
				forkableOffHeapPoolList.remove(currentForkableOffHeapPool);
				currentForkableOffHeapPool = rootForkableOffHeapPool;
			}
			lastUsedForkableOffHeapPoolToFree = currentForkableOffHeapPool;
			return true;
		}
		if (lastUsedForkableOffHeapPoolToFree != null) {
			if (lastUsedForkableOffHeapPoolToFree.freeFromAddress(objAddress)) {
				if (lastUsedForkableOffHeapPoolToFree.isEmpty() && 
						lastUsedForkableOffHeapPoolToFree != rootForkableOffHeapPool) {
					lastUsedForkableOffHeapPoolToFree.free();
					forkableOffHeapPoolList.remove(lastUsedForkableOffHeapPoolToFree);
					currentForkableOffHeapPool = rootForkableOffHeapPool;
					lastUsedForkableOffHeapPoolToFree = currentForkableOffHeapPool;
				}
			}
		}
		for (int i = 0; i < forkableOffHeapPoolList.size(); i++) {
			DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool = 
					forkableOffHeapPoolList.get(i);
			if (forkableOffHeapPool != currentForkableOffHeapPool) {
				if (forkableOffHeapPool.freeFromAddress(objAddress)) {
					if (forkableOffHeapPool.isEmpty() && forkableOffHeapPool != rootForkableOffHeapPool) {
						forkableOffHeapPool.free();
						forkableOffHeapPoolList.remove(forkableOffHeapPool);
						currentForkableOffHeapPool = rootForkableOffHeapPool;
						lastUsedForkableOffHeapPoolToFree = currentForkableOffHeapPool;
					} 
					else {
						lastUsedForkableOffHeapPoolToFree = forkableOffHeapPool;
					}	
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public synchronized void reset() {
		for (int i = 0; i < forkableOffHeapPoolList.size(); i++) {
			DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool = 
					forkableOffHeapPoolList.get(i);
			if (forkableOffHeapPool != rootForkableOffHeapPool) {
				forkableOffHeapPool.reset();
			}	
		}
		rootForkableOffHeapPool.reset();
		init();
		makeAvaiable();
	}
	
	@Override
	public synchronized void free() {
		checkAvailability();
		for (int i = 0; i < forkableOffHeapPoolList.size(); i++) {
			DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool = 
					forkableOffHeapPoolList.get(i);
			if (forkableOffHeapPool != rootForkableOffHeapPool) {
				forkableOffHeapPool.free();
			}	
		}
		rootForkableOffHeapPool.free();
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
