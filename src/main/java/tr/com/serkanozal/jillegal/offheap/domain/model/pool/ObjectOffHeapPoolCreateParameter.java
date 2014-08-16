/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;

public class ObjectOffHeapPoolCreateParameter<T> extends BaseOffHeapPoolCreateParameter<T> {

	protected long objectCount;
	protected ObjectPoolReferenceType referenceType;
	protected NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType = 
					NonPrimitiveFieldAllocationConfigType.ONLY_CONFIGURED_NON_PRIMITIVE_FIELDS;
	
	public ObjectOffHeapPoolCreateParameter(Class<T> elementType, long objectCount, 
			ObjectPoolReferenceType referenceType) {
		super(OffHeapPoolType.OBJECT_POOL, elementType);
		this.objectCount = objectCount;
		this.referenceType = referenceType;
	}
	
	public ObjectOffHeapPoolCreateParameter(Class<T> elementType, long objectCount, 
			ObjectPoolReferenceType referenceType, DirectMemoryService offHeapMemoryService) {
		super(OffHeapPoolType.OBJECT_POOL, elementType, offHeapMemoryService);
		this.objectCount = objectCount;
		this.referenceType = referenceType;
	}
	
	public ObjectOffHeapPoolCreateParameter(Class<T> elementType, long objectCount, 
			ObjectPoolReferenceType referenceType, boolean makeOffHeapableAsAuto) {
		super(OffHeapPoolType.OBJECT_POOL, elementType, makeOffHeapableAsAuto);
		this.objectCount = objectCount;
		this.referenceType = referenceType;
	}
	
	public ObjectOffHeapPoolCreateParameter(Class<T> elementType, long objectCount, 
			ObjectPoolReferenceType referenceType, boolean makeOffHeapableAsAuto, 
			DirectMemoryService offHeapMemoryService) {
		super(OffHeapPoolType.OBJECT_POOL, elementType, offHeapMemoryService, makeOffHeapableAsAuto);
		this.objectCount = objectCount;
		this.referenceType = referenceType;
	}
	
	public ObjectOffHeapPoolCreateParameter(Class<T> elementType, long objectCount, 
			ObjectPoolReferenceType referenceType, 
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType, 
			boolean makeOffHeapableAsAuto) {
		super(OffHeapPoolType.OBJECT_POOL, elementType, makeOffHeapableAsAuto);
		this.objectCount = objectCount;
		this.referenceType = referenceType;
		this.allocateNonPrimitiveFieldsAtOffHeapConfigType = allocateNonPrimitiveFieldsAtOffHeapConfigType;
	}
	
	public ObjectOffHeapPoolCreateParameter(Class<T> elementType, long objectCount, 
			ObjectPoolReferenceType referenceType, 
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType, 
			boolean makeOffHeapableAsAuto, DirectMemoryService offHeapMemoryService) {
		super(OffHeapPoolType.OBJECT_POOL, elementType, offHeapMemoryService, makeOffHeapableAsAuto);
		this.objectCount = objectCount;
		this.referenceType = referenceType;
		this.allocateNonPrimitiveFieldsAtOffHeapConfigType = allocateNonPrimitiveFieldsAtOffHeapConfigType;
	}
	
	public long getObjectCount() {
		return objectCount;
	}
	
	public void setObjectCount(long objectCount) {
		this.objectCount = objectCount;
	}
	
	public ObjectPoolReferenceType getReferenceType() {
		return referenceType;
	}
	
	public void setReferenceType(ObjectPoolReferenceType referenceType) {
		this.referenceType = referenceType;
	}
	
	public NonPrimitiveFieldAllocationConfigType getAllocateNonPrimitiveFieldsAtOffHeapConfigType() {
		return allocateNonPrimitiveFieldsAtOffHeapConfigType;
	}
	
	public void setAllocateNonPrimitiveFieldsAtOffHeapConfigType(
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType) {
		this.allocateNonPrimitiveFieldsAtOffHeapConfigType = allocateNonPrimitiveFieldsAtOffHeapConfigType;
	}
	
}
