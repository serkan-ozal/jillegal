/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;

public class DefaultForkableObjectOffHeapPool<T> extends LazyReferencedObjectOffHeapPool<T> {

	public static final int DEFAULT_OBJECT_COUNT = 1000;

	public DefaultForkableObjectOffHeapPool(Class<T> elementType, DirectMemoryService directMemoryService) {
		super(elementType, DEFAULT_OBJECT_COUNT, directMemoryService);
	}
	
	public DefaultForkableObjectOffHeapPool(Class<T> elementType) {
		super(elementType, DEFAULT_OBJECT_COUNT, DirectMemoryServiceFactory.getDirectMemoryService());
	}

}
