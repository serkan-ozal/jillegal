/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.interceptor;

public class InterceptorServiceFactory {

	private static InterceptorService interceptorService = new InterceptorServiceImpl();
	
	private InterceptorServiceFactory() {
		
	}
	
	public static InterceptorService getInterceptorService() {
		return interceptorService;
	}
	
	public static void setInterceptorService(InterceptorService service) {
		interceptorService = service;
	}
	
	public static void notifyBeforeConstructorInterceptors(Object obj, String constructorSignature, Object[] params) {
		interceptorService.notifyBeforeConstructorInterceptors(obj, constructorSignature, params);
	}
	
	public static void notifyAfterConstructorInterceptors(Object obj, String constructorSignature, Object [] params) {
		interceptorService.notifyAfterConstructorInterceptors(obj, constructorSignature, params);
	}
	 
	public static void notifyBeforeMethodInterceptors(Object obj, String methodSignature, Object [] params) {
		interceptorService.notifyBeforeMethodInterceptors(obj, methodSignature, params);
	}
	
	public static void notifyAfterMethodInterceptors(Object obj, String methodSignature, Object [] params) {
		interceptorService.notifyAfterMethodInterceptors(obj, methodSignature, params);
	}

}
