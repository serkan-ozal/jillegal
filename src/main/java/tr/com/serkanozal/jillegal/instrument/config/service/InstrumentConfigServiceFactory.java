/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.config.service;

import tr.com.serkanozal.jillegal.instrument.config.service.annotation.AnnotationBasedInstrumentConfigService;

public class InstrumentConfigServiceFactory {

	private static InstrumentConfigService instrumentConfigService = new AnnotationBasedInstrumentConfigService();
	
	private InstrumentConfigServiceFactory() {
		
	}
	
	public static InstrumentConfigService getInstrumentConfigService() {
		return instrumentConfigService;
	}
	
	public static void setInstrumentConfigService(InstrumentConfigService instrumentConfigService) {
		InstrumentConfigServiceFactory.instrumentConfigService = instrumentConfigService;
	}
	
}
