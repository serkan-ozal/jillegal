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
		redefineWithAgent(cls, byteCodes);
	}
	
	public <T> void redefineWithAgent(Class<T> cls, byte[] byteCodes) {
		JillegalAgent.redefineClass(cls, byteCodes);
	}
	
	/*
	// TODO In development
	@SuppressWarnings({ "unchecked" })
	public <T> void redefineWithUnsafe(Class<T> cls, byte[] byteCodes) {
		try {
			DirectMemoryServiceImpl impl = new DirectMemoryServiceImpl();
			Class<T> newClass = (Class<T>) new InstrumentationClassLoader().buildClass(byteCodes);
			newClass.newInstance();
			Unsafe u = JvmUtil.getUnsafe();
			long oldClassAddress1 = impl.addressOfClass(cls);
			long oldClassAddress2 = impl.addressOf(cls);
			
			long newClassAddress1 = impl.addressOfClass(newClass);
			long newClassAddress2 = impl.addressOf(newClass);
			
			System.out.println(Long.toHexString(oldClassAddress2) + ", " + Long.toHexString(oldClassAddress1));
			System.out.println(Long.toHexString(newClassAddress2) + ", " + Long.toHexString(newClassAddress1));

			for (int i = 0; i < 8 * 40; i++) {
				if (i % 16 == 0) {
					System.out.print(String.format("[%02d]: ", i));
				}
		    	System.out.print(String.format("%02x ", u.getByte((long)oldClassAddress2 + i)));
				if ((i + 1) % 16 == 0) {
					System.out.println();
				}
	    	}	
	    	System.out.println();
			
			for (int i = 0; i < 8 * 40; i++) {
				if (i % 16 == 0) {
					System.out.print(String.format("[%04d]: ", i));
				}
		    	System.out.print(String.format("%02x ", u.getByte((long)oldClassAddress1 + i)));
				if ((i + 1) % 16 == 0) {
					System.out.println();
				}
	    	}	
	    	System.out.println();

	    	u.copyMemory(newClassAddress1, oldClassAddress1, 690);
		}
		catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	private class InstrumentationClassLoader extends ClassLoader {
		
		public InstrumentationClassLoader() {
			super(InstrumentationClassLoader.class.getClassLoader());
		}
		
		@SuppressWarnings("deprecation")
		public Class<?> buildClass(byte[] byteCode) {
			return defineClass(byteCode, 0, byteCode.length);
		}
		
	}
	*/

	@Override
	public <T> void redefineClass(GeneratedClass<T> generatedClass) {
		redefineClass(generatedClass.getSourceClass(), generatedClass.getClassData());
	}

}
