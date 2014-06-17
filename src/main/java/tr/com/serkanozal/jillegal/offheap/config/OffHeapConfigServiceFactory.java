/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.config;

public class OffHeapConfigServiceFactory {

	private static OffHeapConfigService offHeapConfigService = new OffHeapConfigServiceImpl();
	
	private OffHeapConfigServiceFactory() {
		
	}
	
	public static OffHeapConfigService getOffHeapConfigService() {
		return offHeapConfigService;
	}
	
	public static void setOffHeapConfigService(OffHeapConfigService offHeapConfigService) {
		OffHeapConfigServiceFactory.offHeapConfigService = offHeapConfigService;
	}
	
}
