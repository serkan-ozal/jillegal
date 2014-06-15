/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.pool;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ExtendableArrayOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.pool.DeeplyForkableArrayOffHeapPool;

public class ExtendableArrayOffHeapPoolCreateParameterBuilder<T, A> implements Builder<ExtendableArrayOffHeapPoolCreateParameter<T, A>> {

	private DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableArrayOffHeapPool;
	private boolean makeOffHeapableAsAuto = true;
	
	@Override
	public ExtendableArrayOffHeapPoolCreateParameter<T, A> build() {
		return new ExtendableArrayOffHeapPoolCreateParameter<T, A>(forkableArrayOffHeapPool, makeOffHeapableAsAuto);
	}
	
	public ExtendableArrayOffHeapPoolCreateParameterBuilder<T, A> forkableArrayOffHeapPool(
			DeeplyForkableArrayOffHeapPool<T, A, ? extends OffHeapPoolCreateParameter<T>> forkableArrayOffHeapPool) {
		this.forkableArrayOffHeapPool = forkableArrayOffHeapPool;
		return this;
	}
	
	public ExtendableArrayOffHeapPoolCreateParameterBuilder<T, A> makeOffHeapableAsAuto(
			boolean makeOffHeapableAsAuto) {
		this.makeOffHeapableAsAuto = makeOffHeapableAsAuto;
		return this;
	}

}
