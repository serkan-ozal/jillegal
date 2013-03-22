/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal;

import tr.com.serkanozal.jillegal.agent.AgentInitializer;
import tr.com.serkanozal.jillegal.instrument.InstrumenterInitializer;

public class Jillegal {

	public static final String GROUP_ID = "tr.com.serkanozal";
	public static final String ARTIFACT_ID = "jillegal";
	public static final String VERSION = "1.1.3-BETA";
	
	private Jillegal() {
		
	}
	
	public static void init() {
		// ***********************************
        // ******* Do not change order *******
        // ***********************************
		InstrumenterInitializer.init();
		AgentInitializer.init();
	}
	
}
