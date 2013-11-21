/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;

public class ObjectOffHeapPoolCreateParameter<T> extends BaseOffHeapPoolCreateParameter<T> {

	protected int objectCount;
	protected ObjectPoolReferenceType referenceType;
	protected boolean autoImplementNonPrimitiveFieldSetters = true;
	
	public ObjectOffHeapPoolCreateParameter(Class<T> elementType, int objectCount, 
			ObjectPoolReferenceType referenceType, boolean autoImplementNonPrimitiveFieldSetters) {
		super(OffHeapPoolType.SEQUENTIAL_OBJECT_POOL, elementType);
		this.objectCount = objectCount;
		this.referenceType = referenceType;
		this.autoImplementNonPrimitiveFieldSetters = autoImplementNonPrimitiveFieldSetters;
	}
	
	public ObjectOffHeapPoolCreateParameter(Class<T> elementType, int objectCount, 
			ObjectPoolReferenceType referenceType, boolean autoImplementNonPrimitiveFieldSetters, 
			DirectMemoryService offHeapMemoryService) {
		super(OffHeapPoolType.SEQUENTIAL_OBJECT_POOL, elementType, offHeapMemoryService);
		this.objectCount = objectCount;
		this.referenceType = referenceType;
		this.autoImplementNonPrimitiveFieldSetters = autoImplementNonPrimitiveFieldSetters;
	}
	
	public int getObjectCount() {
		return objectCount;
	}
	
	public void setObjectCount(int objectCount) {
		this.objectCount = objectCount;
	}
	
	public ObjectPoolReferenceType getReferenceType() {
		return referenceType;
	}
	
	public void setReferenceType(ObjectPoolReferenceType referenceType) {
		this.referenceType = referenceType;
	}
	
	public boolean isAutoImplementNonPrimitiveFieldSetters() {
		return autoImplementNonPrimitiveFieldSetters;
	}
	
	public void setAutoImplementNonPrimitiveFieldSetters(boolean autoImplementNonPrimitiveFieldSetters) {
		this.autoImplementNonPrimitiveFieldSetters = autoImplementNonPrimitiveFieldSetters;
	}
	
}
