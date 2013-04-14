/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.pool;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectPoolReferenceType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.SequentialObjectOffHeapPoolCreateParameter;

public class SequentialObjectOffHeapPoolCreateParameterBuilder<T> implements Builder<SequentialObjectOffHeapPoolCreateParameter<T>> {

	private Class<T> type;
	private int objectCount;
	private ObjectPoolReferenceType referenceType = ObjectPoolReferenceType.LAZY_REFERENCED;
	
	@Override
	public SequentialObjectOffHeapPoolCreateParameter<T> build() {
		return new SequentialObjectOffHeapPoolCreateParameter<T>(type, objectCount, referenceType);
	}
	
	public SequentialObjectOffHeapPoolCreateParameterBuilder<T> type(Class<T> type) {
		this.type = type;
		return this;
	}
	
	public SequentialObjectOffHeapPoolCreateParameterBuilder<T> objectCount(int objectCount) {
		this.objectCount = objectCount;
		return this;
	}
	
	public SequentialObjectOffHeapPoolCreateParameterBuilder<T> referenceType(ObjectPoolReferenceType referenceType) {
		this.referenceType = referenceType;
		return this;
	}

}
