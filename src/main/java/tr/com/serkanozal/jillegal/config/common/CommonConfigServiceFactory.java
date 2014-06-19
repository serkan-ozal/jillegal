/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.config.common;

public class CommonConfigServiceFactory {

	private static CommonConfigService commonConfigService = new CommonConfigServiceImpl();
	
	private CommonConfigServiceFactory() {
		
	}
	
	public static CommonConfigService getCommonConfigService() {
		return commonConfigService;
	}
	
	public static void setCommonConfigService(CommonConfigService commonConfigService) {
		CommonConfigServiceFactory.commonConfigService = commonConfigService;
	}
	
}
