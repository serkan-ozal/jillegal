/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;

public abstract class BaseOffHeapPool<T, P extends OffHeapPoolCreateParameter<T>> implements OffHeapPool<T, P> {

	protected Class<T> elementType;
	protected DirectMemoryService directMemoryService;
	
	public BaseOffHeapPool(Class<T> elementType) {
		if (elementType == null) {
			throw new IllegalArgumentException("\"elementType\" cannot be null !");
		}
		this.elementType = elementType;
		this.directMemoryService = DirectMemoryServiceFactory.getDirectMemoryService();
	}
	
	public BaseOffHeapPool(Class<T> elementType, DirectMemoryService directMemoryService) {
		if (elementType == null) {
			throw new IllegalArgumentException("\"elementType\" cannot be null !");
		}
		this.elementType = elementType;
		this.directMemoryService = directMemoryService != null ? directMemoryService : DirectMemoryServiceFactory.getDirectMemoryService();
	}
	
	@Override
	public Class<T> getElementType() {
		return elementType;
	}
	
	public DirectMemoryService getDirectMemoryService() {
		return directMemoryService;
	}
	
}
