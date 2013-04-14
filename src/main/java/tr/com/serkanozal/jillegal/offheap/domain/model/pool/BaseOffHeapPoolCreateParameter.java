/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;

public abstract class BaseOffHeapPoolCreateParameter<T> implements OffHeapPoolCreateParameter<T> {

	protected OffHeapPoolType offHeapPoolType;
	protected Class<T> elementType;
	protected DirectMemoryService directMemoryService;
	
	public BaseOffHeapPoolCreateParameter(OffHeapPoolType offHeapPoolType, Class<T> elementType) {
		this.offHeapPoolType = offHeapPoolType;
		this.elementType = elementType;
	}
	
	public BaseOffHeapPoolCreateParameter(OffHeapPoolType offHeapPoolType, Class<T> elementType, 
			DirectMemoryService directMemoryService) {
		this.offHeapPoolType = offHeapPoolType;
		this.elementType = elementType;
		this.directMemoryService = directMemoryService;
	}
	
	@Override
	public OffHeapPoolType getOffHeapPoolType() {
		return offHeapPoolType;
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
