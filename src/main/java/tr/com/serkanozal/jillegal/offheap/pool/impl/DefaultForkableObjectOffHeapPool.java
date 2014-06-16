/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapConstants;

public class DefaultForkableObjectOffHeapPool<T> extends LazyReferencedObjectOffHeapPool<T> {

	public DefaultForkableObjectOffHeapPool(Class<T> elementType) {
		super(elementType, 
				OffHeapConstants.DEFAULT_OBJECT_COUNT, 
				OffHeapConstants.DEFAULT_NON_PRIMITIVE_FIELD_ALLOCATION_CONFIG_TYPE, 
				DirectMemoryServiceFactory.getDirectMemoryService());
	}
	
	public DefaultForkableObjectOffHeapPool(Class<T> elementType, int objectCount) {
		super(elementType, objectCount, 
				OffHeapConstants.DEFAULT_NON_PRIMITIVE_FIELD_ALLOCATION_CONFIG_TYPE, 
				DirectMemoryServiceFactory.getDirectMemoryService());
	}
	
	public DefaultForkableObjectOffHeapPool(Class<T> elementType, int objectCount,
			boolean allocateNonPrimitiveFieldsAtOffHeap) {
		super(elementType, objectCount, 
				OffHeapConstants.DEFAULT_NON_PRIMITIVE_FIELD_ALLOCATION_CONFIG_TYPE, 
				DirectMemoryServiceFactory.getDirectMemoryService());
	}
	
	public DefaultForkableObjectOffHeapPool(Class<T> elementType, DirectMemoryService directMemoryService) {
		super(elementType, OffHeapConstants.DEFAULT_OBJECT_COUNT, 
				OffHeapConstants.DEFAULT_NON_PRIMITIVE_FIELD_ALLOCATION_CONFIG_TYPE, directMemoryService);
	}
	
	public DefaultForkableObjectOffHeapPool(Class<T> elementType, int objectCount, 
			DirectMemoryService directMemoryService) {
		super(elementType, objectCount, 
				OffHeapConstants.DEFAULT_NON_PRIMITIVE_FIELD_ALLOCATION_CONFIG_TYPE, 
				directMemoryService);
	}
	
	public DefaultForkableObjectOffHeapPool(Class<T> elementType, int objectCount, 
			boolean allocateNonPrimitiveFieldsAtOffHeap, DirectMemoryService directMemoryService) {
		super(elementType, objectCount, 
				OffHeapConstants.DEFAULT_NON_PRIMITIVE_FIELD_ALLOCATION_CONFIG_TYPE, 
				directMemoryService);
	}

}
