/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;

public abstract class BaseOffHeapPool<T, P extends OffHeapPoolCreateParameter<T>> implements RandomAccessOffHeapPool<T, P> {

	protected Class<T> elementType;
	protected DirectMemoryService directMemoryService;
	
	public BaseOffHeapPool(Class<T> elementType, DirectMemoryService directMemoryService) {
		this.elementType = elementType;
		this.directMemoryService = directMemoryService;
	}
	
	public Class<T> getElementType() {
		return elementType;
	}
	
	public DirectMemoryService getDirectMemoryService() {
		return directMemoryService;
	}
	
}
