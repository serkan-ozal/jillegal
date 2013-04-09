/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;

public class SequentialObjectPoolCreateParameter<T> extends BaseOffHeapPoolCreateParameter<T> {

	public enum SequentialObjectPoolReferenceType {
		LAZY_REFERENCED,
		EAGER_REFERENCED
	}
	
	protected long objectCount;
	protected SequentialObjectPoolReferenceType referenceType;
	
	public SequentialObjectPoolCreateParameter(Class<T> elementType, long objectCount, 
			SequentialObjectPoolReferenceType referenceType) {
		super(OffHeapPoolType.SEQUENTIAL_OBJECT_POOL, elementType);
		this.objectCount = objectCount;
		this.referenceType = referenceType;
	}
	
	public SequentialObjectPoolCreateParameter(Class<T> elementType, long objectCount, 
			SequentialObjectPoolReferenceType referenceType, DirectMemoryService offHeapMemoryService) {
		super(OffHeapPoolType.SEQUENTIAL_OBJECT_POOL, elementType, offHeapMemoryService);
		this.objectCount = objectCount;
		this.referenceType = referenceType;
	}
	
	public long getObjectCount() {
		return objectCount;
	}
	
	public void setObjectCount(long objectCount) {
		this.objectCount = objectCount;
	}
	
	public SequentialObjectPoolReferenceType getReferenceType() {
		return referenceType;
	}
	
	public void setReferenceType(SequentialObjectPoolReferenceType referenceType) {
		this.referenceType = referenceType;
	}
	
}
