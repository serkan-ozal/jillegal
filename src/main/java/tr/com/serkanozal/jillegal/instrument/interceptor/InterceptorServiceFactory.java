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
	
	public void setInterceptorService(InterceptorService service) {
		interceptorService = service;
	}
	
}
