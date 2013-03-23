/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.config.service;

import tr.com.serkanozal.jillegal.offheap.config.service.annotation.AnnotationBasedOffHeapConfigService;

public class OffHeapConfigServiceFactory {

	private static OffHeapConfigService offHeapConfigService = new AnnotationBasedOffHeapConfigService();
	
	private OffHeapConfigServiceFactory() {
		
	}
	
	public static OffHeapConfigService getOffHeapConfigService() {
		return offHeapConfigService;
	}
	
	public static void setOffHeapConfigService(OffHeapConfigService offHeapConfigService) {
		OffHeapConfigServiceFactory.offHeapConfigService = offHeapConfigService;
	}
	
}
