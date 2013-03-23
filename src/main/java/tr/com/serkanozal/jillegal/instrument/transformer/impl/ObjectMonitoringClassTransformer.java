/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.transformer.impl;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import javassist.ClassPool;

public class ObjectMonitoringClassTransformer implements ClassFileTransformer  {

	protected static Map<String, String> transformedClassMap = new HashMap<String, String>();
	
	protected final Logger logger = Logger.getLogger(getClass());
	protected ClassPool cp = ClassPool.getDefault();
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, 
			ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
		// TODO Implement
		return bytes;
	}

}
