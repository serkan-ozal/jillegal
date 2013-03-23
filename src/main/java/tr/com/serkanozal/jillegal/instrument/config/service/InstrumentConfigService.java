/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.config.service;

import tr.com.serkanozal.jillegal.instrument.config.model.MonitorableConfigBean;

public interface InstrumentConfigService {

	MonitorableConfigBean getMonitorableConfig(byte[] byteCode);
	
}
