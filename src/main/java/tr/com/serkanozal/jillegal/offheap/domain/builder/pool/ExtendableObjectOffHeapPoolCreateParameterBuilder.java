/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.pool;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ExtendableObjectOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableObjectOffHeapPool;

public class ExtendableObjectOffHeapPoolCreateParameterBuilder<T> implements Builder<ExtendableObjectOffHeapPoolCreateParameter<T>> {

	private DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableObjectOffHeapPool;
	
	@Override
	public ExtendableObjectOffHeapPoolCreateParameter<T> build() {
		return new ExtendableObjectOffHeapPoolCreateParameter<T>(forkableObjectOffHeapPool);
	}
	
	public ExtendableObjectOffHeapPoolCreateParameterBuilder<T> forkableObjectOffHeapPool(
			DeeplyForkableObjectOffHeapPool<T, ? extends OffHeapPoolCreateParameter<T>> forkableObjectOffHeapPool) {
		this.forkableObjectOffHeapPool = forkableObjectOffHeapPool;
		return this;
	}

}
