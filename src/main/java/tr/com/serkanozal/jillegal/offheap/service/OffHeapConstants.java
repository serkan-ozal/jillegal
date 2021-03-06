/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.service;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;

public interface OffHeapConstants {

	int DEFAULT_OBJECT_COUNT = Integer.getInteger("jillegal.offheap.pool.objectCount", 1024);
	NonPrimitiveFieldAllocationConfigType DEFAULT_NON_PRIMITIVE_FIELD_ALLOCATION_CONFIG_TYPE = 
			NonPrimitiveFieldAllocationConfigType.getDefault();
	int DEFAULT_ESTIMATED_STRING_COUNT = Integer.getInteger("jillegal.offheap.pool.estimatedStringCount", 1024);
	int DEFAULT_ESTIMATED_STRING_LENGTH = Integer.getInteger("jillegal.offheap.pool.estimatedStringLength", 32);
	
}
