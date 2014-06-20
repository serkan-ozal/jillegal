/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.config.provider.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OffHeapAware {
	
	NonPrimitiveFieldAllocationConfigType nonPrimitiveFieldAllocationConfigType() 
		default NonPrimitiveFieldAllocationConfigType.ONLY_CONFIGURED_NON_PRIMITIVE_FIELDS;
	
}
