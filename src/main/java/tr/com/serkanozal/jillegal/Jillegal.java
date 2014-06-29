/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal;

import tr.com.serkanozal.jillegal.initializer.AgentInitializer;
import tr.com.serkanozal.jillegal.instrument.initializer.InstrumentInitializer;
import tr.com.serkanozal.jillegal.offheap.initializer.OffHeapInitializer;
import tr.com.serkanozal.jillegal.util.JillegalAwarer;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public class Jillegal {

	public static final String GROUP_ID = "tr.com.serkanozal";
	public static final String ARTIFACT_ID = "jillegal";
	public static String VERSION = "1.0.6-RELEASE";
	
	private static boolean initialized = false;
	
	static {
		init();
	}

	private Jillegal() {
		
	}
	
	public synchronized static void init() {
		if (initialized == false) {
			JvmUtil.info();
			
			AgentInitializer.init();
			InstrumentInitializer.init();
			OffHeapInitializer.init();
			JillegalAwarer.makeJillegalAware();
			initialized = true;
		}	
	}
	
}
