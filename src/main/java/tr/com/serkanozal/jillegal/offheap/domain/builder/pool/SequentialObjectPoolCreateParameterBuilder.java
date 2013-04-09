/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.pool;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.SequentialObjectPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.SequentialObjectPoolCreateParameter.SequentialObjectPoolReferenceType;

public class SequentialObjectPoolCreateParameterBuilder<T> implements Builder<SequentialObjectPoolCreateParameter<T>> {

	private Class<T> type;
	private long objectCount;
	private SequentialObjectPoolReferenceType referenceType = SequentialObjectPoolReferenceType.LAZY_REFERENCED;
	
	@Override
	public SequentialObjectPoolCreateParameter<T> build() {
		return new SequentialObjectPoolCreateParameter<T>(type, objectCount, referenceType);
	}
	
	public SequentialObjectPoolCreateParameterBuilder<T> type(Class<T> type) {
		this.type = type;
		return this;
	}
	
	public SequentialObjectPoolCreateParameterBuilder<T> objectCount(long objectCount) {
		this.objectCount = objectCount;
		return this;
	}
	
	public SequentialObjectPoolCreateParameterBuilder<T> referenceType(SequentialObjectPoolReferenceType referenceType) {
		this.referenceType = referenceType;
		return this;
	}

}
