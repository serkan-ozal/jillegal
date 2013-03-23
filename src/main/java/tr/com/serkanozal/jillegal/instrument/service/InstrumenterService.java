/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.service;

import tr.com.serkanozal.jillegal.instrument.Instrumenter;
import tr.com.serkanozal.jillegal.instrument.domain.model.GeneratedClass;
import tr.com.serkanozal.jillegal.instrument.factory.InstrumenterFactory;

public interface InstrumenterService {

	InstrumenterFactory getDefaultInstrumenterFactory();
	void setDefaultInstrumenterFactory(InstrumenterFactory instrumenterFactory);
	
	<T> InstrumenterFactory getInstrumenterFactory(Class<T> clazz);
	<T> void setInstrumenterFactory(InstrumenterFactory instrumenterFactory, Class<T> clazz);
	
	<T> Instrumenter<T> getInstrumenter(Class<T> clazz);
	
	<T> void redefineClass(Class<T> cls, byte[] byteCodes);
	<T> void redefineClass(GeneratedClass<T> generatedClass);

}
