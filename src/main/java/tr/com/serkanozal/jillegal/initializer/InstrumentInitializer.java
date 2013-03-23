/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.initializer;

import tr.com.serkanozal.jillegal.agent.JillegalAgent;
import tr.com.serkanozal.jillegal.instrument.transformer.impl.DefaultClassTransformer;

public class InstrumentInitializer {

	private InstrumentInitializer() {
		
	}
	
	public static void init() {
		JillegalAgent.getInstrumentation().addTransformer(new DefaultClassTransformer());
	}
	
}
