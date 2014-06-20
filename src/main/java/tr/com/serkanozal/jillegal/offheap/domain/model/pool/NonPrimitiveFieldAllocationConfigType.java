/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

public enum NonPrimitiveFieldAllocationConfigType {

	NONE,
	ALL_NON_PRIMITIVE_FIELDS,
	ONLY_CONFIGURED_NON_PRIMITIVE_FIELDS;
	
	public static final NonPrimitiveFieldAllocationConfigType DEFAULT = ONLY_CONFIGURED_NON_PRIMITIVE_FIELDS;
	
	public static NonPrimitiveFieldAllocationConfigType getDefault() {
		return DEFAULT;
	}
	
}
