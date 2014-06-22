/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.pool;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ExtendableStringOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableStringOffHeapPool;

public class ExtendableStringOffHeapPoolCreateParameterBuilder 
		implements Builder<ExtendableStringOffHeapPoolCreateParameter> {

	private DeeplyForkableStringOffHeapPool forkableStringOffHeapPool;
	
	@Override
	public ExtendableStringOffHeapPoolCreateParameter build() {
		return new ExtendableStringOffHeapPoolCreateParameter(forkableStringOffHeapPool);
	}
	
	public ExtendableStringOffHeapPoolCreateParameterBuilder forkableStringOffHeapPool(
			DeeplyForkableStringOffHeapPool forkableStringOffHeapPool) {
		this.forkableStringOffHeapPool = forkableStringOffHeapPool;
		return this;
	}

}
