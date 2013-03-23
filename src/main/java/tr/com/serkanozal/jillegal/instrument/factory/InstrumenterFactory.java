/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.factory;

import tr.com.serkanozal.jillegal.instrument.Instrumenter;

public interface InstrumenterFactory {

	<T> Instrumenter<T> createInstrumenter(Class<T> clazz);
	
}
