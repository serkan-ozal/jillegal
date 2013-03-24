/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;

public abstract class BaseOffHeapPoolCreateParameter<T> implements OffHeapPoolCreateParameter<T> {

	protected OffHeapPoolType poolType;
	protected Class<T> elementType;
	protected DirectMemoryService directMemoryService;
	
	public BaseOffHeapPoolCreateParameter(OffHeapPoolType poolType, Class<T> elementType) {
		this.poolType = poolType;
		this.elementType = elementType;
	}
	
	public BaseOffHeapPoolCreateParameter(OffHeapPoolType poolType, Class<T> elementType, 
			DirectMemoryService directMemoryService) {
		this.poolType = poolType;
		this.elementType = elementType;
		this.directMemoryService = directMemoryService;
	}
	
	@Override
	public OffHeapPoolType getPoolType() {
		return poolType;
	}
	
	@Override
	public Class<T> getElementType() {
		return elementType;
	}
	
	public DirectMemoryService getDirectMemoryService() {
		return directMemoryService;
	}
	
	public void setDirectMemoryService(DirectMemoryService directMemoryService) {
		this.directMemoryService = directMemoryService;
	}

}
