/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;

public class SequentialObjectOffHeapPoolCreateParameter<T> extends BaseOffHeapPoolCreateParameter<T> {

	protected int objectCount;
	protected ObjectPoolReferenceType referenceType;
	
	public SequentialObjectOffHeapPoolCreateParameter(Class<T> elementType, int objectCount, 
			ObjectPoolReferenceType referenceType) {
		super(OffHeapPoolType.SEQUENTIAL_OBJECT_POOL, elementType);
		this.objectCount = objectCount;
		this.referenceType = referenceType;
	}
	
	public SequentialObjectOffHeapPoolCreateParameter(Class<T> elementType, int objectCount, 
			ObjectPoolReferenceType referenceType, DirectMemoryService offHeapMemoryService) {
		super(OffHeapPoolType.SEQUENTIAL_OBJECT_POOL, elementType, offHeapMemoryService);
		this.objectCount = objectCount;
		this.referenceType = referenceType;
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
	
}
