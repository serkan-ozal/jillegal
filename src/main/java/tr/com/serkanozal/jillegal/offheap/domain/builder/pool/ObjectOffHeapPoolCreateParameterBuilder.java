/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.pool;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectPoolReferenceType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectOffHeapPoolCreateParameter;

public class ObjectOffHeapPoolCreateParameterBuilder<T> implements Builder<ObjectOffHeapPoolCreateParameter<T>> {

	private Class<T> type;
	private int objectCount;
	private ObjectPoolReferenceType referenceType = ObjectPoolReferenceType.LAZY_REFERENCED;
	private boolean makeOffHeapableAsAuto = true;
	private NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType = 
				NonPrimitiveFieldAllocationConfigType.ONLY_CONFIGURED_NON_PRIMITIVE_FIELDS;
	
	@Override
	public ObjectOffHeapPoolCreateParameter<T> build() {
		return 
			new ObjectOffHeapPoolCreateParameter<T>(
					type, 
					objectCount, 
					referenceType, 
					allocateNonPrimitiveFieldsAtOffHeapConfigType,
					makeOffHeapableAsAuto);
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
	
	public ObjectOffHeapPoolCreateParameterBuilder<T> makeOffHeapableAsAuto(
			boolean makeOffHeapableAsAuto) {
		this.makeOffHeapableAsAuto = makeOffHeapableAsAuto;
		return this;
	}
	
	public ObjectOffHeapPoolCreateParameterBuilder<T> allocateNonPrimitiveFieldsAtOffHeapConfigType(
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType) {
		this.allocateNonPrimitiveFieldsAtOffHeapConfigType = allocateNonPrimitiveFieldsAtOffHeapConfigType;
		return this;
	}

}
