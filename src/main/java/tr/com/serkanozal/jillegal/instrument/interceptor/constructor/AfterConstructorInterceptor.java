/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.interceptor.constructor;

import java.lang.reflect.Constructor;

public interface AfterConstructorInterceptor<T> {
	
    void afterConstructor(T obj, Constructor<T> constructor, Object [] params);
    
}
