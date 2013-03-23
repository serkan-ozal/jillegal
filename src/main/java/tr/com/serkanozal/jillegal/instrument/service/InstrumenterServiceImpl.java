/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jillegal.agent.JillegalAgent;
import tr.com.serkanozal.jillegal.instrument.Instrumenter;
import tr.com.serkanozal.jillegal.instrument.domain.model.GeneratedClass;
import tr.com.serkanozal.jillegal.instrument.factory.DefaultInstrumenterFactory;
import tr.com.serkanozal.jillegal.instrument.factory.InstrumenterFactory;

public class InstrumenterServiceImpl implements InstrumenterService {

	private final Logger logger = Logger.getLogger(getClass());
	
	private InstrumenterFactory defaultInstrumenterFactory = new DefaultInstrumenterFactory();
	private Map<Class<?>, InstrumenterFactory> instrumenterFactoryMap = new HashMap<Class<?>, InstrumenterFactory>();
	
	public InstrumenterServiceImpl() {
		init();
	}
	
	protected void init() {

	}
	
	protected InstrumenterFactory findInstrumenterFactory(Class<?> clazz) {
		InstrumenterFactory instrumenterFactory = instrumenterFactoryMap.get(clazz);
		if (instrumenterFactory != null) {
			return instrumenterFactory;
		}
		else {
			return defaultInstrumenterFactory;
		}
	}
	
	@Override
	public InstrumenterFactory getDefaultInstrumenterFactory() {
		return defaultInstrumenterFactory;
	}
	
	@Override
	public void setDefaultInstrumenterFactory(InstrumenterFactory instrumenterFactory) {
		defaultInstrumenterFactory = instrumenterFactory;
	}
	
	@Override
	public <T> InstrumenterFactory getInstrumenterFactory(Class<T> clazz) {
		return instrumenterFactoryMap.get(clazz);
	}
	
	@Override
	public <T> void setInstrumenterFactory(InstrumenterFactory instrumenterFactory, Class<T> clazz) {
		instrumenterFactoryMap.put(clazz, instrumenterFactory);
	}
	
	@Override
	public <T> Instrumenter<T> getInstrumenter(Class<T> clazz) {
		InstrumenterFactory instrumenterFactory = findInstrumenterFactory(clazz);
		if (instrumenterFactory != null) {
			return instrumenterFactory.createInstrumenter(clazz);
		}
		else {
			logger.warn("Instrumenter couldn't be found for class " + clazz.getName());
			return null;
		}
	}
	
	@Override
	public <T> void redefineClass(Class<T> cls, byte[] byteCodes) {
		JillegalAgent.redefineClass(cls, byteCodes);
	}

	@Override
	public <T> void redefineClass(GeneratedClass<T> generatedClass) {
		redefineClass(generatedClass.getSourceClass(), generatedClass.getClassData());
	}

}
