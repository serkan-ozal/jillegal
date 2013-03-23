/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.service;

public class InstrumenterServiceFactory {

	private static InstrumenterService instrumenterService = new InstrumenterServiceImpl();
	
	private InstrumenterServiceFactory() {
		
	}
	
	public static InstrumenterService getInstrumenterService() {
		return instrumenterService;
	}
	
	public static void setInstrumenterService(InstrumenterService instrumenterService) {
		InstrumenterServiceFactory.instrumenterService = instrumenterService;
	}

}
