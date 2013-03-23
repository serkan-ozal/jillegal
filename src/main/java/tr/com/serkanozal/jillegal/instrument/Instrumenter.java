/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument;

import tr.com.serkanozal.jillegal.instrument.domain.model.GeneratedClass;
import tr.com.serkanozal.jillegal.instrument.interceptor.clazz.ClassInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.constructor.AfterConstructorInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.constructor.BeforeConstructorInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.method.AfterMethodInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.method.BeforeMethodInterceptor;

public interface Instrumenter<T> {
	
    Instrumenter<T> addAdditionalClass(Class<?> additionalClass) throws Exception; 
    
    Instrumenter<T> addConstructor(String code, Class<?> ... paramTypes) throws Exception; 
    Instrumenter<T> updateConstructor(String code, Class<?> ... paramTypes) throws Exception;
    Instrumenter<T> deleteConstructor(Class<?> ... paramTypes) throws Exception;
    Instrumenter<T> insertBeforeConstructors(String code) throws Exception; 
    Instrumenter<T> insertAfterConstructors(String code) throws Exception; 
    Instrumenter<T> insertBeforeConstructors(BeforeConstructorInterceptor<T> interceptor) throws Exception; 
    Instrumenter<T> insertAfterConstructors(AfterConstructorInterceptor<T> interceptor) throws Exception; 
    Instrumenter<T> insertBeforeConstructor(String code, Class<?> ... paramTypes) throws Exception; 
    Instrumenter<T> insertAfterConstructor(String code, Class<?> ... paramTypes) throws Exception;
    Instrumenter<T> insertBeforeConstructor(BeforeConstructorInterceptor<T> interceptor, Class<?> ... paramTypes) throws Exception; 
    Instrumenter<T> insertAfterConstructor(AfterConstructorInterceptor<T> interceptor, Class<?> ... paramTypes) throws Exception;   
    
    Instrumenter<T> addMethod(String methodName, Class<?> returnType, String code, Class<?> ... paramTypes) throws Exception;
    Instrumenter<T> updateMethod(String methodName, String code, Class<?> ... paramTypes) throws Exception;
    Instrumenter<T> deleteMethod( String methodName, Class<?> ... paramTypes) throws Exception;
    Instrumenter<T> insertBeforeMethods(String code) throws Exception; 
    Instrumenter<T> insertAfterMethods(String code) throws Exception; 
    Instrumenter<T> insertBeforeMethods(BeforeMethodInterceptor<T> interceptor) throws Exception; 
    Instrumenter<T> insertAfterMethods(AfterMethodInterceptor<T> interceptor) throws Exception; 
    Instrumenter<T> insertBeforeMethod(String methodName, String code, Class<?> ... paramTypes) throws Exception; 
    Instrumenter<T> insertAfterMethod(String methodName, String code, Class<?> ... paramTypes) throws Exception; 
    Instrumenter<T> insertBeforeMethod(String methodName, BeforeMethodInterceptor<T> interceptor, Class<?> ... paramTypes) throws Exception; 
    Instrumenter<T> insertAfterMethod(String methodName, AfterMethodInterceptor<T> interceptor, Class<?> ... paramTypes) throws Exception; 
    
    Instrumenter<T> addClassInterceptor(ClassInterceptor<T> interceptor) throws Exception; 
    
    GeneratedClass<T> build() throws Exception; 
    void saveToFile() throws Exception; 
    void saveToFile(String dirName) throws Exception; 
    
}
