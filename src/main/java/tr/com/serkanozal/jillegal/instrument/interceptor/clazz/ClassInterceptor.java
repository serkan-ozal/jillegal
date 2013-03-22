/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.interceptor.clazz;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import tr.com.serkanozal.jillegal.instrument.interceptor.constructor.ConstructorInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.method.MethodInterceptor;

public interface ClassInterceptor<T> extends ConstructorInterceptor<T>, MethodInterceptor<T> {
	
    void classLoaded(Class<T> clazz);
    void beforeConstructor(T obj, Constructor<T> constructor, Object [] params);
    void afterConstructor(T obj, Constructor<T> constructor, Object [] params);
    void beforeMethod(T obj, Method method, Object [] params);
    Object afterMethod(T obj, Method method, Object [] params);
    
}
