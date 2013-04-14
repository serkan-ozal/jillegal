/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableObjectOffHeapPool;

public class ExtendableObjectOffHeapPoolCreateParameter<T> extends BaseOffHeapPoolCreateParameter<T> {

	protected DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableObjectOffHeapPool;

	public ExtendableObjectOffHeapPoolCreateParameter( 
			DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableObjectOffHeapPool) {
		super(OffHeapPoolType.EXTENDABLE_OBJECT_POOL, forkableObjectOffHeapPool.getElementType());
		this.forkableObjectOffHeapPool = forkableObjectOffHeapPool;
	}
	
	public DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> getForkableObjectOffHeapPool() {
		return forkableObjectOffHeapPool;
	}
	
	public void setForkableObjectOffHeapPool(
			DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableObjectOffHeapPool) {
		this.forkableObjectOffHeapPool = forkableObjectOffHeapPool;
	}
	
}
