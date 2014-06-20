/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.service;

public class InstrumentServiceFactory {

	private static InstrumentService instrumentService = new InstrumentServiceImpl();
	
	private InstrumentServiceFactory() {
		
	}
	
	public static InstrumentService getInstrumentService() {
		return instrumentService;
	}
	
	public static void setInstrumentService(InstrumentService instrumentService) {
		InstrumentServiceFactory.instrumentService = instrumentService;
	}

}
