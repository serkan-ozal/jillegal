/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.interceptor.constructor;

import java.lang.reflect.Constructor;

public interface ConstructorInterceptor<T> extends BeforeConstructorInterceptor<T>, AfterConstructorInterceptor<T> {
   
	void beforeConstructor(T obj, Constructor<T> constructor, Object [] params);
    void afterConstructor(T obj, Constructor<T> constructor, Object [] params);

}
