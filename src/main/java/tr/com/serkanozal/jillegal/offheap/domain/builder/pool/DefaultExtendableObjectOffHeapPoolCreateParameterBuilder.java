/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.pool;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ExtendableObjectOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.pool.impl.DefaultForkableObjectOffHeapPool;

public class DefaultExtendableObjectOffHeapPoolCreateParameterBuilder<T> implements Builder<ExtendableObjectOffHeapPoolCreateParameter<T>> {

	private Class<T> elementType;
	
	@Override
	public ExtendableObjectOffHeapPoolCreateParameter<T> build() {
		return new ExtendableObjectOffHeapPoolCreateParameter<T>(new DefaultForkableObjectOffHeapPool<T>(elementType));
	}
	
	public DefaultExtendableObjectOffHeapPoolCreateParameterBuilder<T> elementType(Class<T> elementType) {
		this.elementType = elementType;
		return this;
	}

}
