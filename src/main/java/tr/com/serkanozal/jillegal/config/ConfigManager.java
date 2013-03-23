/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.config;

import tr.com.serkanozal.jillegal.instrument.config.service.InstrumentConfigService;
import tr.com.serkanozal.jillegal.instrument.config.service.InstrumentConfigServiceFactory;
import tr.com.serkanozal.jillegal.offheap.config.service.OffHeapConfigService;
import tr.com.serkanozal.jillegal.offheap.config.service.OffHeapConfigServiceFactory;

public class ConfigManager {

	private ConfigManager() {
		
	}
	
	public static InstrumentConfigService getInstrumentConfigService() {
		return InstrumentConfigServiceFactory.getInstrumentConfigService();
	}
	
	public static OffHeapConfigService getOffHeapConfigService() {
		return OffHeapConfigServiceFactory.getOffHeapConfigService();
	}
	
}
