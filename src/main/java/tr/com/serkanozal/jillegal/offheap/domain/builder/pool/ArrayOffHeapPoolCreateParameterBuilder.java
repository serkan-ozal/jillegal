/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.pool;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ArrayOffHeapPoolCreateParameter;

public class ArrayOffHeapPoolCreateParameterBuilder<T> implements Builder<ArrayOffHeapPoolCreateParameter<T>> {

	private Class<T> type;
	private int length;
	private boolean usePrimitiveTypes;
	private boolean initializeElements;
	
	@Override
	public ArrayOffHeapPoolCreateParameter<T> build() {
		return new ArrayOffHeapPoolCreateParameter<T>(type, length, usePrimitiveTypes, initializeElements);
	}
	
	public ArrayOffHeapPoolCreateParameterBuilder<T> type(Class<T> type) {
		this.type = type;
		return this;
	}
	
	public ArrayOffHeapPoolCreateParameterBuilder<T> length(int length) {
		this.length = length;
		return this;
	}
	
	public ArrayOffHeapPoolCreateParameterBuilder<T> usePrimitiveTypes(boolean usePrimitiveTypes) {
		this.usePrimitiveTypes = usePrimitiveTypes;
		return this;
	}
	
	public ArrayOffHeapPoolCreateParameterBuilder<T> initializeElements(boolean initializeElements) {
		this.initializeElements = initializeElements;
		return this;
	}

}
