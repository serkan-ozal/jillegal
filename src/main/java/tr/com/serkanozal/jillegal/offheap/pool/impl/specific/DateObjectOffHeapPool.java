/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl.specific;

import java.sql.Date;

import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.pool.impl.LazyReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapConstants;

public class DateObjectOffHeapPool extends LazyReferencedObjectOffHeapPool<Date> {

	public DateObjectOffHeapPool() {
		super(	Date.class, 
				OffHeapConstants.DEFAULT_OBJECT_COUNT, 
				OffHeapConstants.DEFAULT_NON_PRIMITIVE_FIELD_ALLOCATION_CONFIG_TYPE, 
				DirectMemoryServiceFactory.getDirectMemoryService());
	}
	
	public DateObjectOffHeapPool(int objectCount) {
		super(	Date.class, 
				objectCount, 
				OffHeapConstants.DEFAULT_NON_PRIMITIVE_FIELD_ALLOCATION_CONFIG_TYPE, 
				DirectMemoryServiceFactory.getDirectMemoryService());
	}

	public DateObjectOffHeapPool(DirectMemoryService directMemoryService) {
		super(	Date.class, 
				OffHeapConstants.DEFAULT_OBJECT_COUNT, 
				OffHeapConstants.DEFAULT_NON_PRIMITIVE_FIELD_ALLOCATION_CONFIG_TYPE, 
				directMemoryService);
	}
	
	public DateObjectOffHeapPool(int objectCount, DirectMemoryService directMemoryService) {
		super(	Date.class, 
				objectCount, 
				OffHeapConstants.DEFAULT_NON_PRIMITIVE_FIELD_ALLOCATION_CONFIG_TYPE, 
				directMemoryService);
	}
	
	// TODO Implement as specific to "java.util.Date"
	
}
