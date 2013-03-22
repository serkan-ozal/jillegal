/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.interceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tr.com.serkanozal.jillegal.instrument.interceptor.clazz.ClassInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.constructor.AfterConstructorInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.constructor.BeforeConstructorInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.method.AfterMethodInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.method.BeforeMethodInterceptor;

public class InterceptorServiceImpl implements InterceptorService {
	
    final static public int BEFORE_CONSTRUCTOR_INTERCEPTOR = 1;
    final static public int AFTER_CONSTRUCTOR_INTERCEPTOR = 2;
    final static public int BEFORE_METHOD_INTERCEPTOR = 3;
    final static public int AFTER_METHOD_INTERCEPTOR = 4;
    
    private Map<String, List<BeforeConstructorInterceptor<?>>> beforeConstructorInterceptors = 
                    new HashMap<String, List<BeforeConstructorInterceptor<?>>>( );
    
    private Map<String, List<AfterConstructorInterceptor<?>>> afterConstructorInterceptors = 
                    new HashMap<String, List<AfterConstructorInterceptor<?>>>( );
    
    private Map<String, List<BeforeMethodInterceptor<?>>> beforeMethodInterceptors = 
                    new HashMap<String, List<BeforeMethodInterceptor<?>>>( );
    
    private Map<String, List<AfterMethodInterceptor<?>>> afterMethodInterceptors = 
                    new HashMap<String, List<AfterMethodInterceptor<?>>>( );
    
    private Map<Class<?>, List<ClassInterceptor<?>>> classInterceptors = 
                    new HashMap<Class<?>, List<ClassInterceptor<?>>>( );
    
    @Override
    public void addBeforeConstructorInterceptor(Class<?> clazz, String constructorSignature, 
                                                BeforeConstructorInterceptor<?> interceptor) {
        List<BeforeConstructorInterceptor<?>> interceptors = beforeConstructorInterceptors.get(constructorSignature);
        if (interceptors == null) {
            interceptors = new ArrayList<BeforeConstructorInterceptor<?>>();
            beforeConstructorInterceptors.put(constructorSignature, interceptors);
        }
        interceptors.add(interceptor);
    }
    
    @Override
    public void addAfterConstructorInterceptor(Class<?> clazz, String constructorSignature, 
                                               AfterConstructorInterceptor<?> interceptor) {
        List<AfterConstructorInterceptor<?>> interceptors = afterConstructorInterceptors.get(constructorSignature);
        if (interceptors == null) {
            interceptors = new ArrayList<AfterConstructorInterceptor<?>>();
            afterConstructorInterceptors.put(constructorSignature, interceptors);
        }
        interceptors.add(interceptor);
    }
    
    @Override
    public void addBeforeConstructorsInterceptor(Class<?> clazz, BeforeConstructorInterceptor<?> interceptor) {
        String signature = clazz.getName() + "#" + clazz.getSimpleName();
        List<BeforeConstructorInterceptor<?>> interceptors = beforeConstructorInterceptors.get(signature);
        if (interceptors == null) {
            interceptors = new ArrayList<BeforeConstructorInterceptor<?>>();
            beforeConstructorInterceptors.put(signature, interceptors);
        }
        interceptors.add(interceptor);
    }
    
    @Override
    public void addAfterConstructorsInterceptor(Class<?> clazz, AfterConstructorInterceptor<?> interceptor) {
        String signature = clazz.getName() + "#" + clazz.getSimpleName();
        List<AfterConstructorInterceptor<?>> interceptors = afterConstructorInterceptors.get(signature);
        if (interceptors == null) {
            interceptors = new ArrayList<AfterConstructorInterceptor<?>>();
            afterConstructorInterceptors.put(signature, interceptors);
        }
        interceptors.add(interceptor);
    }
    
    @Override
    public void addBeforeMethodInterceptor(Class<?> clazz, String methodSignature, BeforeMethodInterceptor<?> interceptor) {
        List<BeforeMethodInterceptor<?>> interceptors = beforeMethodInterceptors.get(methodSignature);
        if (interceptors == null) {
            interceptors = new ArrayList<BeforeMethodInterceptor<?>>();
            beforeMethodInterceptors.put(methodSignature, interceptors);
        }
        interceptors.add(interceptor);
    }
    
    @Override
    public void addAfterMethodInterceptor(Class<?> clazz, String methodSignature, AfterMethodInterceptor<?> interceptor) {
        List<AfterMethodInterceptor<?>> interceptors = afterMethodInterceptors.get(methodSignature);
        if (interceptors == null) {
            interceptors = new ArrayList<AfterMethodInterceptor<?>>();
            afterMethodInterceptors.put(methodSignature, interceptors);
        }
        interceptors.add(interceptor);
    }
    
    @Override
    public void addBeforeMethodsInterceptor(Class<?> clazz, BeforeMethodInterceptor<?> interceptor) {
        String signature = clazz.getName() + "#";
        List<BeforeMethodInterceptor<?>> interceptors = beforeMethodInterceptors.get(signature);
        if (interceptors == null) {
            interceptors = new ArrayList<BeforeMethodInterceptor<?>>();
            beforeMethodInterceptors.put(signature, interceptors);
        }
        interceptors.add(interceptor);
    }
    
    @Override
    public void addAfterMethodsInterceptor(Class<?> clazz, AfterMethodInterceptor<?> interceptor) {
        String signature = clazz.getName() + "#";
        List<AfterMethodInterceptor<?>> interceptors = afterMethodInterceptors.get(signature);
        if (interceptors == null) {
            interceptors = new ArrayList<AfterMethodInterceptor<?>>();
            afterMethodInterceptors.put(signature, interceptors);
        }
        interceptors.add(interceptor);
    }
    
    @Override
    public void notifyBeforeConstructorInterceptors(Object obj, String constructorSignature, Object[] params) {
        notifyConstructorInterceptors(obj, constructorSignature, true, params);
        notifyBeforeConstructorsInterceptors(obj, params);
    }
    
    @Override
    public void notifyAfterConstructorInterceptors(Object obj, String constructorSignature, Object [] params) {
        notifyConstructorInterceptors(obj, constructorSignature, false, params);
        notifyAfterConstructorsInterceptors(obj, params);
    }
    
    @Override
    public void notifyBeforeConstructorsInterceptors(Object obj, Object [] params) {
        String signature = obj.getClass().getName() + "#" + obj.getClass().getSimpleName();
        notifyConstructorInterceptors(obj, signature, true, params);
    }
    
    @Override
    public void notifyAfterConstructorsInterceptors(Object obj, Object [] params) {
        String signature = obj.getClass().getName() + "#" + obj.getClass().getSimpleName();
        notifyConstructorInterceptors(obj, signature, false, params);
    }
    
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private void notifyConstructorInterceptors(Object obj, String constructorSignature, boolean isBefore, Object [] params) {
        List<BeforeConstructorInterceptor<?>> beforeInterceptors = null;
        List<AfterConstructorInterceptor<?>> afterInterceptors = null;
        
        if (isBefore) {
            beforeInterceptors = beforeConstructorInterceptors.get(constructorSignature);
        }    
        else {
            afterInterceptors = afterConstructorInterceptors.get(constructorSignature);
        }
        
        if (beforeInterceptors != null || afterInterceptors != null) {
            constructorSignature = constructorSignature.replaceAll("[\\s]+", "");
            try {
                Constructor[] constructors = null;
                
                int paramsStart = constructorSignature.indexOf("(");
                int paramsEnd = constructorSignature.indexOf(")");
                
                if (paramsStart > 0 && paramsEnd > 0) {
                    String paramsSignature = constructorSignature.substring(paramsStart + 1, paramsEnd).trim();
                    String[] paramTypeNames = paramsSignature.split( "," );
                    int size = paramTypeNames == null ? 0 : paramTypeNames.length;
                    Class[] paramTypes = new Class<?>[size];
                    if (size == 1 && paramTypeNames[0].trim().length() == 0) {
                        paramTypeNames  = null;
                        paramTypes      = new Class<?>[0];
                    }    
                    if (paramTypeNames != null) {
                        for (int i = 0; i < paramTypeNames.length; i++) {
                            paramTypes[i] = Class.forName(paramTypeNames[i].trim());
                        }    
                    }       
                    Constructor c = obj.getClass().getDeclaredConstructor(paramTypes);
                    constructors = new Constructor[] { c };
                }    
                else {
                    constructors = obj.getClass().getDeclaredConstructors();
                }
                
                if (isBefore) {
                    if (beforeInterceptors != null) {
                        for (BeforeConstructorInterceptor i : beforeInterceptors) {
                            for (int j = 0; j < constructors.length; j++) {
                                i.beforeConstructor(obj, constructors[j], params);
                                notifyClassInterceptors(obj.getClass(), BEFORE_CONSTRUCTOR_INTERCEPTOR, 
                                                        obj, constructors[j], null, params);
                            }    
                        }    
                    }
                }
                else {
                    if (afterInterceptors != null) {
                        for (AfterConstructorInterceptor i : afterInterceptors)
                        {
                            for (int j = 0; j < constructors.length; j++) {
                                i.afterConstructor(obj, constructors[j], params);
                                notifyClassInterceptors(obj.getClass(), AFTER_CONSTRUCTOR_INTERCEPTOR, 
                                                        obj, constructors[j], null, params);
                            }    
                        }    
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void notifyBeforeMethodInterceptors(Object obj, String methodSignature, Object [] params) {
        notifyMethodInterceptors(obj, methodSignature, true, params);
        if (obj != null) {
            notifyBeforeMethodsInterceptors(obj, params);
        }    
        else {
            notifyBeforeMethodsInterceptorsWithSignature(methodSignature, params);
        }    
    }
    
    @Override
    public void notifyAfterMethodInterceptors(Object obj, String methodSignature, Object [] params) {
        notifyMethodInterceptors(obj, methodSignature, false, params);
        if (obj != null) {
            notifyAfterMethodsInterceptors(obj, params);
        }    
        else {
            notifyAfterMethodsInterceptorsWithSignature(methodSignature, params);
        }    
    }
    
    @Override
    public void notifyBeforeMethodsInterceptors(Object obj, Object [] params) {
        String signature = obj.getClass().getName() + "#";
        notifyMethodInterceptors(obj, signature, true, params);
    }
    
    @Override
    public void notifyBeforeMethodsInterceptorsWithSignature(String methodSignature, Object [] params) {
        String signature = methodSignature.substring(0, methodSignature.lastIndexOf(".") ) + "#"; 
        notifyMethodInterceptors(null, signature, true, params);
    }
    
    @Override
    public void notifyAfterMethodsInterceptors(Object obj, Object [] params) {
        String signature = obj.getClass().getName() + "#";
        notifyMethodInterceptors(obj, signature, false, params);
    }
    
    @Override
    public void notifyAfterMethodsInterceptorsWithSignature(String methodSignature, Object [] params) {
        String signature = methodSignature.substring(0, methodSignature.lastIndexOf(".")) + "#"; 
        notifyMethodInterceptors(null, signature, false, params);
    }
    
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private void notifyMethodInterceptors(Object obj, String methodSignature, boolean isBefore, Object [] params) {
        List<BeforeMethodInterceptor<?>> beforeInterceptors = null;
        List<AfterMethodInterceptor<?>> afterInterceptors = null;

        if (isBefore) {
            beforeInterceptors = beforeMethodInterceptors.get(methodSignature);
        }    
        else {
            afterInterceptors = afterMethodInterceptors.get(methodSignature);
        }    
        Class<?> objClass = null;
        if (obj != null) {
            objClass = obj.getClass();
        }    
        else {
            String className = methodSignature.substring(0, methodSignature.lastIndexOf("."));
            try {
                objClass = Class.forName(className);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        if (beforeInterceptors != null || afterInterceptors != null) {
            methodSignature = methodSignature.replaceAll("[\\s]+", "");
            try {
                Method[] methods = null;
                
                int paramsStart = methodSignature.indexOf("(");
                int paramsEnd = methodSignature.indexOf(")");
                
                if (paramsStart > 0 && paramsEnd > 0) {
                    int lastDotIndex = methodSignature.lastIndexOf( "#" );
                    String methodName = methodSignature.substring( lastDotIndex + 1, paramsStart );
                    String paramsSignature = methodSignature.substring( paramsStart + 1, paramsEnd ).trim( );
                    String[] paramTypeNames = paramsSignature.split( "," );
                    int size = paramTypeNames == null ? 0 : paramTypeNames.length;
                    Class[] paramTypes = new Class<?>[size];
                    if (size == 1 && paramTypeNames[0].trim().length() == 0) {
                        paramTypeNames = null;
                        paramTypes = new Class<?>[0];
                    }    
                    if (paramTypeNames != null) {
                        for (int i = 0; i < paramTypeNames.length; i++) {
                            paramTypes[i] = Class.forName(paramTypeNames[i].trim());
                        }    
                    }    
                    Method m = objClass.getDeclaredMethod(methodName, paramTypes);
                    methods = new Method[] { m };
                }    
                else {
                    methods = objClass.getDeclaredMethods();
                }    
                
                if (isBefore) {
                    if (beforeInterceptors != null) {
                        for (BeforeMethodInterceptor i : beforeInterceptors) {
                            for (int j = 0; j < methods.length; j++) {
                                i.beforeMethod(obj, methods[j], params);
                                notifyClassInterceptors(objClass, BEFORE_METHOD_INTERCEPTOR, 
                                                        obj, null, methods[j], params);
                            }    
                        }    
                    }
                }
                else {
                    if (afterInterceptors != null) {
                        for (AfterMethodInterceptor i : afterInterceptors) {
                            for (int j = 0; j < methods.length; j++) {
                                i.afterMethod(obj, methods[j], params);
                                notifyClassInterceptors(objClass, AFTER_METHOD_INTERCEPTOR, 
                                                        obj, null, methods[j], params);
                            }    
                        }    
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void addClassInterceptor(Class<?> clazz, ClassInterceptor<?> interceptor) {
        List<ClassInterceptor<?>> interceptors = classInterceptors.get(clazz);
        if (interceptors == null) {
            interceptors = new ArrayList<ClassInterceptor<?>>();
            classInterceptors.put(clazz, interceptors);
        }
        interceptors.add(interceptor);
    }
    
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Override
    public void notifyClassInterceptors(Class<?> clazz, int interceptionType, Object obj, 
                                        Constructor<?> c, Method m, Object [] params) {
        List<ClassInterceptor<?>> interceptors = classInterceptors.get(clazz);
        if (interceptors != null) {
           for (ClassInterceptor i : interceptors) {
               switch (interceptionType) {
                   case BEFORE_CONSTRUCTOR_INTERCEPTOR:
                       i.beforeConstructor(obj, c, params);
                       break;
                   case AFTER_CONSTRUCTOR_INTERCEPTOR:
                       i.afterConstructor(obj, c, params);
                       break;
                   case BEFORE_METHOD_INTERCEPTOR:
                       i.beforeMethod(obj, m, params);
                       break;
                   case AFTER_METHOD_INTERCEPTOR:
                       i.afterMethod(obj, m, params);
                       break;    
               }
           }
        }    
    }    
    
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Override
    public void notifyClassInterceptors(Class clazz) {
        List<ClassInterceptor<?>> interceptors = classInterceptors.get(clazz);
        if (interceptors != null) {
           for (ClassInterceptor<?> i : interceptors) {
               i.classLoaded(clazz);
           }    
        }    
    }    
    
}
