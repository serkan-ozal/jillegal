/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.pool;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectPoolReferenceType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectOffHeapPoolCreateParameter;

public class ObjectOffHeapPoolCreateParameterBuilder<T> implements Builder<ObjectOffHeapPoolCreateParameter<T>> {

	private Class<T> type;
	private int objectCount;
	private ObjectPoolReferenceType referenceType = ObjectPoolReferenceType.LAZY_REFERENCED;
	private boolean autoImplementNonPrimitiveFieldSetters = true;
	
	@Override
	public ObjectOffHeapPoolCreateParameter<T> build() {
		return new ObjectOffHeapPoolCreateParameter<T>(type, objectCount, referenceType, 
						autoImplementNonPrimitiveFieldSetters);
	}
	
	public ObjectOffHeapPoolCreateParameterBuilder<T> type(Class<T> type) {
		this.type = type;
		return this;
	}
	
	public ObjectOffHeapPoolCreateParameterBuilder<T> objectCount(int objectCount) {
		this.objectCount = objectCount;
		return this;
	}
	
	public ObjectOffHeapPoolCreateParameterBuilder<T> referenceType(ObjectPoolReferenceType referenceType) {
		this.referenceType = referenceType;
		return this;
	}
	
	public ObjectOffHeapPoolCreateParameterBuilder<T> autoImplementNonPrimitiveFieldSetters(
			boolean autoImplementNonPrimitiveFieldSetters) {
		this.autoImplementNonPrimitiveFieldSetters = autoImplementNonPrimitiveFieldSetters;
		return this;
	}

}
