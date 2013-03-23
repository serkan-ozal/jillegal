/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.config.model;

import tr.com.serkanozal.jillegal.instrument.interceptor.clazz.ClassInterceptor;

@SuppressWarnings("rawtypes")
public class MonitorableConfigBean {

	private Class<? extends ClassInterceptor> classInterceptor;
	
	public MonitorableConfigBean() {
		
	}
	
	public MonitorableConfigBean(Class<? extends ClassInterceptor> classInterceptor) {
		this.classInterceptor = classInterceptor;
	}
	
	public Class<? extends ClassInterceptor> getClassInterceptor() {
		return classInterceptor;
	}
	
	public void setClassInterceptor(Class<? extends ClassInterceptor> classInterceptor) {
		this.classInterceptor = classInterceptor;
	}
	
}
