/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.interceptor.method;

import java.lang.reflect.Method;

public interface BeforeMethodInterceptor<T> {
    
	void beforeMethod(T obj, Method method, Object [] params);

}
