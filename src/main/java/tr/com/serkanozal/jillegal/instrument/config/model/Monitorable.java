/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.config.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import tr.com.serkanozal.jillegal.instrument.interceptor.clazz.ClassInterceptor;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("rawtypes")
public @interface Monitorable {
	
	Class<? extends ClassInterceptor> classInterceptor() default ClassInterceptor.class;
	
}
