/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.transformer.impl;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import tr.com.serkanozal.jillegal.agent.JillegalAgent;
import tr.com.serkanozal.jillegal.config.annotation.JillegalAware;
import tr.com.serkanozal.jillegal.instrument.transformer.ClassTransformer;
import tr.com.serkanozal.jillegal.util.JillegalAwarer;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;

public class JillegalAwareClassTransformer implements ClassTransformer {

	protected static final Set<String> transformedClasses = new HashSet<String>();
	protected static final ClassPool cp = ClassPool.getDefault();
	
	public JillegalAwareClassTransformer() {
		init();
	}
	
	protected void init() {
		 cp.importPackage(JillegalAwarer.class.getPackage().getName());
         cp.appendClassPath(new ClassClassPath(JillegalAwarer.class));
	}
    
	protected boolean isJillegalAware(CtClass clazz) {
		return clazz.hasAnnotation(JillegalAware.class);
	}
	
	protected byte[] transform(CtClass ct) {
		try {
	    	if (isJillegalAware(ct)) {
	            System.out.println("[INFO] : " + "Class " + ct.getName() + " is being instrumented ...");
	            
	            CtConstructor[] constructors = ct.getConstructors();
	            
	            for (CtConstructor c : constructors) {
	            	c.insertAfter("JillegalAwarer.doJillegalAware(this);");
	            }
	            
	            return ct.toBytecode();
	    	}
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
		
		return null;
	}

	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, 
			ProtectionDomain domain, byte[] bytes) throws IllegalClassFormatException {
		if (transformedClasses.contains(className)) {
			return bytes;
		}
        try {
        	byte[] instrumentedBytes = transform(cp.makeClass(new ByteArrayInputStream(bytes), false));
        	if (instrumentedBytes != null) {
        		return instrumentedBytes;
        	}
        }
        catch (Throwable t) {
        	t.printStackTrace();
        }
        return bytes;
    }
	
	public void transform(Class<?> classBeingTransformed) {
		try {
			JillegalAgent.getInstrumentation().
				redefineClasses(
					new ClassDefinition(
							classBeingTransformed, 
							transform(cp.get(classBeingTransformed.getName()))));
		} 
		catch (Throwable t) {
			t.printStackTrace();
		} 
	}
	
}
