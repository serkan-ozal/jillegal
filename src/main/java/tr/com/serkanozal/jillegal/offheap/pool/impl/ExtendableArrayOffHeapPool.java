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
import tr.com.serkanozal.jillegal.offheap.pool.ContentAwareOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableArrayOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ArrayOffHeapPool;

public class ExtendableArrayOffHeapPool<T, A> extends BaseOffHeapPool<T, ExtendableArrayOffHeapPoolCreateParameter<T, A>> 
		implements 	ArrayOffHeapPool<T, A, ExtendableArrayOffHeapPoolCreateParameter<T, A>>,
					ContentAwareOffHeapPool<T, ExtendableArrayOffHeapPoolCreateParameter<T, A>> {

	protected DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> rootForkableOffHeapPool;
	protected List<DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>>> forkableOffHeapPoolList = 
					new ArrayList<DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>>>();
	protected DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> currentForkableOffHeapPool;
	protected long totalLength;	
	protected int length;
	
	public ExtendableArrayOffHeapPool(ExtendableArrayOffHeapPoolCreateParameter<T, A> parameter) {
		this(parameter.getElementType(), parameter.getForkableArrayOffHeapPool(), parameter.getDirectMemoryService());
	}
	
	public ExtendableArrayOffHeapPool(Class<T> clazz, 
			DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool, 
			DirectMemoryService directMemoryService) {
		super(clazz, directMemoryService);
		init(forkableOffHeapPool);
	}
	
	@Override
	protected void init() {
		super.init();
		
		currentForkableOffHeapPool = rootForkableOffHeapPool;
		length = rootForkableOffHeapPool.getLength();
		totalLength = rootForkableOffHeapPool.getLength();
	}

	@Override
	public synchronized A getArray() {
		checkAvailability();
		return currentForkableOffHeapPool.getArray();
	}
	
	@Override
	public int getLength() {
		return Integer.MAX_VALUE;
	}
	
	@Override
	public synchronized long getArrayAsAddress() {
		checkAvailability();
		return currentForkableOffHeapPool.getArrayAsAddress();
	}
	
	@Override
	public synchronized T getAt(int index) {
		checkAvailability();
		if (index < 0 || index > getLength()) {
			return null;
		}
		extendUntil(index);
		return 
			getArrayOffHeapPoolAt(index).
				getAt(index - (index / rootForkableOffHeapPool.getLength()));
	}

	@Override
	public synchronized void setAt(T element, int index) {
		checkAvailability();
		if (index > 0 && index < getLength()) {
			extendUntil(index);
			getArrayOffHeapPoolAt(index).
				setAt(element, index - (index / rootForkableOffHeapPool.getLength()));
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
		for (DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool : forkableOffHeapPoolList) {
			if (forkableOffHeapPool != currentForkableOffHeapPool && forkableOffHeapPool instanceof ContentAwareOffHeapPool) {
				if (((ContentAwareOffHeapPool)forkableOffHeapPool).isMine(address)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public synchronized void reset() {
		for (DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool : forkableOffHeapPoolList) {
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
		for (DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool : forkableOffHeapPoolList) {
			if (forkableOffHeapPool != rootForkableOffHeapPool) {
				forkableOffHeapPool.free();
			}	
		}
		rootForkableOffHeapPool.free();
		makeUnavaiable();
	}

	@Override
	public synchronized void init(ExtendableArrayOffHeapPoolCreateParameter<T, A> parameter) {
		init(parameter.getForkableArrayOffHeapPool());
	}
	
	protected void init(DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableOffHeapPool) {
		rootForkableOffHeapPool = forkableOffHeapPool;
		init();
		makeAvaiable();
	}
	
	protected ArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> getArrayOffHeapPoolAt(int index) {
		extendUntil(index);
		return forkableOffHeapPoolList.get(index / rootForkableOffHeapPool.getLength());
	}
	
	protected void extendUntil(int index) {
		while (totalLength <= index) {
			extend();
		}
	}
	
	protected void extend() {
		DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> newForkableOffHeapPool = 
				currentForkableOffHeapPool.fork();
		forkableOffHeapPoolList.add(currentForkableOffHeapPool);
		currentForkableOffHeapPool = 
				(DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>>) newForkableOffHeapPool;
		totalLength += currentForkableOffHeapPool.getLength();
	}

}
