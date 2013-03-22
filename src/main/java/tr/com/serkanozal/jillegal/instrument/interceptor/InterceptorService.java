/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.interceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import tr.com.serkanozal.jillegal.instrument.interceptor.clazz.ClassInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.constructor.AfterConstructorInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.constructor.BeforeConstructorInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.method.AfterMethodInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.method.BeforeMethodInterceptor;

public interface InterceptorService {

	 void addBeforeConstructorInterceptor(Class<?> clazz, String constructorSignature, 
             							  BeforeConstructorInterceptor<?> interceptor);
	 void addAfterConstructorInterceptor(Class<?> clazz, String constructorSignature, 
             							 AfterConstructorInterceptor<?> interceptor);
	 void addBeforeConstructorsInterceptor(Class<?> clazz, BeforeConstructorInterceptor<?> interceptor);
	 void addAfterConstructorsInterceptor(Class<?> clazz, AfterConstructorInterceptor<?> interceptor);
	 
	 void addBeforeMethodInterceptor(Class<?> clazz, String methodSignature, BeforeMethodInterceptor<?> interceptor);
	 void addAfterMethodInterceptor(Class<?> clazz, String methodSignature, AfterMethodInterceptor<?> interceptor);
	 void addBeforeMethodsInterceptor(Class<?> clazz, BeforeMethodInterceptor<?> interceptor);
	 void addAfterMethodsInterceptor(Class<?> clazz, AfterMethodInterceptor<?> interceptor);
	 
	 void notifyBeforeConstructorInterceptors(Object obj, String constructorSignature, Object[] params);
	 void notifyAfterConstructorInterceptors(Object obj, String constructorSignature, Object [] params);
	 void notifyBeforeConstructorsInterceptors(Object obj, Object [] params);
	 void notifyAfterConstructorsInterceptors(Object obj, Object [] params);
	 
	 void notifyBeforeMethodInterceptors(Object obj, String methodSignature, Object [] params);
	 void notifyAfterMethodInterceptors(Object obj, String methodSignature, Object [] params);
	 void notifyBeforeMethodsInterceptors(Object obj, Object [] params);
	 void notifyBeforeMethodsInterceptorsWithSignature(String methodSignature, Object [] params);
	 void notifyAfterMethodsInterceptors(Object obj, Object [] params);
	 void notifyAfterMethodsInterceptorsWithSignature(String methodSignature, Object [] params);
	 
	 void addClassInterceptor(Class<?> clazz, ClassInterceptor<?> interceptor);
	 void notifyClassInterceptors(Class<?> clazz, int interceptionType, Object obj, 
             					  Constructor<?> c, Method m, Object [] params);
	 void notifyClassInterceptors(Class<?> clazz);
	 
}
