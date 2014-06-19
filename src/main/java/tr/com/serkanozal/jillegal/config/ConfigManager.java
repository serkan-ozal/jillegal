/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.config;

import tr.com.serkanozal.jillegal.config.common.CommonConfigService;
import tr.com.serkanozal.jillegal.config.common.CommonConfigServiceFactory;
import tr.com.serkanozal.jillegal.instrument.config.InstrumentConfigService;
import tr.com.serkanozal.jillegal.instrument.config.InstrumentConfigServiceFactory;
import tr.com.serkanozal.jillegal.offheap.config.OffHeapConfigService;
import tr.com.serkanozal.jillegal.offheap.config.OffHeapConfigServiceFactory;

public class ConfigManager {

	private ConfigManager() {
		
	}
	
	public static CommonConfigService getCommonConfigService() {
		return CommonConfigServiceFactory.getCommonConfigService();
	}
	
	public static InstrumentConfigService getInstrumentConfigService() {
		return InstrumentConfigServiceFactory.getInstrumentConfigService();
	}
	
	public static OffHeapConfigService getOffHeapConfigService() {
		return OffHeapConfigServiceFactory.getOffHeapConfigService();
	}
	
}
