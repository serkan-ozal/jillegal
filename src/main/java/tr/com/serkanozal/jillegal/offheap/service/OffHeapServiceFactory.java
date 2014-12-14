/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.service;

import java.util.Date;

import tr.com.serkanozal.jillegal.Jillegal;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.specific.DateObjectOffHeapPool;

public class OffHeapServiceFactory {

	static {
		Jillegal.init();
	}
	
	private static OffHeapService offHeapService = new OffHeapServiceImpl();
	
	static {
		init();
	}
	
	private OffHeapServiceFactory() {
		
	}
	
	private static void init() {
		registerSpecificObjectOffHeapPools();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void registerSpecificObjectOffHeapPools() {
		if (offHeapService.isEnable()) {
			offHeapService.setObjectOffHeapPool(Date.class, (ObjectOffHeapPool) new DateObjectOffHeapPool());
		}
	}
	
	public static OffHeapService getOffHeapService() {
		return offHeapService;
	}
	
	public static void setOffHeapService(OffHeapService service) {
		offHeapService = service;
	}
	
}
