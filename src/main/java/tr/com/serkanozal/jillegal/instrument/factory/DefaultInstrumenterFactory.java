/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.factory;

import org.apache.log4j.Logger;

import javassist.NotFoundException;

import tr.com.serkanozal.jillegal.instrument.Instrumenter;
import tr.com.serkanozal.jillegal.instrument.impl.DefaultInstrumenter;

public class DefaultInstrumenterFactory implements InstrumenterFactory {

	private final Logger logger = Logger.getLogger(getClass());
	
	@Override
	public <T> Instrumenter<T> createInstrumenter(Class<T> clazz) {
		try {
			return new DefaultInstrumenter<T>(clazz);
		} 
		catch (NotFoundException e) {
			logger.error("Error at DefaultInstrumenterFactory.createInstrumenter()", e);
			return null;
		}
	}

}
