/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

import tr.com.serkanozal.jillegal.offheap.memory.OffHeapMemoryService;

public class SequentialObjectPoolCreateParameter<T> extends BaseOffHeapPoolCreateParameter<T> {

	protected long objectCount;
	
	public SequentialObjectPoolCreateParameter(Class<T> elementType, long objectCount) {
		super(OffHeapPoolType.SEQUENTIAL_OBJECT_POOL, elementType);
		this.objectCount = objectCount;
	}
	
	public SequentialObjectPoolCreateParameter(Class<T> elementType, long objectCount, OffHeapMemoryService offHeapMemoryService) {
		super(OffHeapPoolType.SEQUENTIAL_OBJECT_POOL, elementType, offHeapMemoryService);
		this.objectCount = objectCount;
	}
	
	public long getObjectCount() {
		return objectCount;
	}
	
	public void setObjectCount(long objectCount) {
		this.objectCount = objectCount;
	}
	
}
