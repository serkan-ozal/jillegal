/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

import tr.com.serkanozal.jillegal.offheap.memory.OffHeapMemoryService;

public abstract class BaseOffHeapPoolCreateParameter<T> implements OffHeapPoolCreateParameter<T> {

	protected OffHeapPoolType poolType;
	protected Class<T> elementType;
	protected OffHeapMemoryService offHeapMemoryService;
	
	public BaseOffHeapPoolCreateParameter(OffHeapPoolType poolType, Class<T> elementType) {
		this.poolType = poolType;
		this.elementType = elementType;
	}
	
	public BaseOffHeapPoolCreateParameter(OffHeapPoolType poolType, Class<T> elementType, 
			OffHeapMemoryService offHeapMemoryService) {
		this.poolType = poolType;
		this.elementType = elementType;
		this.offHeapMemoryService = offHeapMemoryService;
	}
	
	@Override
	public OffHeapPoolType getPoolType() {
		return poolType;
	}
	
	@Override
	public Class<T> getElementType() {
		return elementType;
	}
	
	public OffHeapMemoryService getOffHeapMemoryService() {
		return offHeapMemoryService;
	}
	
	public void setOffHeapMemoryService(OffHeapMemoryService offHeapMemoryService) {
		this.offHeapMemoryService = offHeapMemoryService;
	}

}
