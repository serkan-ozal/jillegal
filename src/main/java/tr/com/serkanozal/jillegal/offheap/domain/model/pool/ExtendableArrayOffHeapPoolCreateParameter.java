/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableArrayOffHeapPool;

public class ExtendableArrayOffHeapPoolCreateParameter<T, A> extends BaseOffHeapPoolCreateParameter<T> {

	protected DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableArrayOffHeapPool;

	public ExtendableArrayOffHeapPoolCreateParameter( 
			DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableArrayOffHeapPool) {
		super(OffHeapPoolType.EXTENDABLE_ARRAY_POOL, forkableArrayOffHeapPool.getElementType());
		this.forkableArrayOffHeapPool = forkableArrayOffHeapPool;
	}
	
	public ExtendableArrayOffHeapPoolCreateParameter( 
			DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableArrayOffHeapPool,
			boolean makeOffHeapableAsAuto) {
		super(OffHeapPoolType.EXTENDABLE_ARRAY_POOL, forkableArrayOffHeapPool.getElementType(), makeOffHeapableAsAuto);
		this.forkableArrayOffHeapPool = forkableArrayOffHeapPool;
	}
	
	public DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> getForkableArrayOffHeapPool() {
		return forkableArrayOffHeapPool;
	}
	
	public void setForkableArrayOffHeapPool(
			DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableArrayOffHeapPool) {
		this.forkableArrayOffHeapPool = forkableArrayOffHeapPool;
	}
	
}
