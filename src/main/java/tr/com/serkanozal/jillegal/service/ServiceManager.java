/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.service;

import tr.com.serkanozal.jillegal.compiler.service.ClassCompilerService;
import tr.com.serkanozal.jillegal.compiler.service.ClassCompilerServiceFactory;
import tr.com.serkanozal.jillegal.instrument.service.InstrumentService;
import tr.com.serkanozal.jillegal.instrument.service.InstrumentServiceFactory;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;

public class ServiceManager {

	private ServiceManager() {
		
	}
	
	public static InstrumentService getInstrumentService() {
		return InstrumentServiceFactory.getInstrumentService();
	}
	
	public static OffHeapService getOffHeapService() {
		return OffHeapServiceFactory.getOffHeapService();
	}
	
	public static ClassCompilerService getClassCompilerService() {
		return ClassCompilerServiceFactory.getClassCompilerService();
	}
	
}
