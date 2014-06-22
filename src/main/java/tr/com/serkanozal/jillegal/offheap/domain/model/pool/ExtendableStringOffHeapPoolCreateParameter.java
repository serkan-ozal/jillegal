/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableStringOffHeapPool;

public class ExtendableStringOffHeapPoolCreateParameter extends BaseOffHeapPoolCreateParameter<String> {

	protected DeeplyForkableStringOffHeapPool forkableStringOffHeapPool;

	public ExtendableStringOffHeapPoolCreateParameter( 
			DeeplyForkableStringOffHeapPool forkableStringOffHeapPool) {
		super(OffHeapPoolType.EXTENDABLE_STRING_POOL, forkableStringOffHeapPool.getElementType());
		this.forkableStringOffHeapPool = forkableStringOffHeapPool;
	}

	public DeeplyForkableStringOffHeapPool getForkableStringOffHeapPool() {
		return forkableStringOffHeapPool;
	}
	
	public void setForkableStringOffHeapPool(
			DeeplyForkableStringOffHeapPool forkableStringOffHeapPool) {
		this.forkableStringOffHeapPool = forkableStringOffHeapPool;
	}
	
}
