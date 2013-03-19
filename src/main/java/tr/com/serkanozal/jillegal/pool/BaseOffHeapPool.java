/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.pool;

import tr.com.serkanozal.jillegal.service.OffHeapMemoryService;

public abstract class BaseOffHeapPool<T> implements RandomAccessOffHeapPool<T> {

	protected Class<T> elementType;
	protected OffHeapMemoryService offHeapMemoryService;
	
	public BaseOffHeapPool(Class<T> elementType, OffHeapMemoryService offHeapMemoryService) {
		this.elementType = elementType;
		this.offHeapMemoryService = offHeapMemoryService;
	}
	
	public Class<T> getElementType() {
		return elementType;
	}
	
	public OffHeapMemoryService getOffHeapMemoryService() {
		return offHeapMemoryService;
	}
	
}
