/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument;

import tr.com.serkanozal.jillegal.instrument.interceptor.InterceptorServiceFactory;
import tr.com.serkanozal.jillegal.instrument.javassist.JavassistInstrumenter;

public class InstrumenterInitializer {
	
    private InstrumenterInitializer() {
        
    }
    
    public static void init() {
        JavassistInstrumenter.class.getClassLoader( );
        InterceptorServiceFactory.class.getClassLoader( );
    }
    
}
