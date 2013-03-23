/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.transformer.impl;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jillegal.instrument.config.model.Monitorable;
import tr.com.serkanozal.jillegal.instrument.domain.model.ClassInfo;
import tr.com.serkanozal.jillegal.instrument.filter.ClassTransformerFilter;
import tr.com.serkanozal.jillegal.instrument.filter.impl.AnnotatedClassTransformerFilter;
import tr.com.serkanozal.jillegal.instrument.transformer.ClassTransformer;

public class DefaultClassTransformer implements ClassTransformer {
	
	final static protected String[] IGNORE = new String[] { "sun/", "com/sun/", "java/", "javax/" };
	
	protected final Logger logger = Logger.getLogger(getClass());
	protected List<ClassTransformerFilter> transformerFilters = new ArrayList<ClassTransformerFilter>();
    
    public DefaultClassTransformer() {
        init();
    }
    
    protected void init() {
    	transformerFilters.add(new AnnotatedClassTransformerFilter(Monitorable.class, new ObjectMonitoringClassTransformer()));
    }
    
    public DefaultClassTransformer(List<ClassTransformerFilter> transformerFilters) {
        this.transformerFilters = transformerFilters;
    }
    
    public List<ClassTransformerFilter> getTransformerFilters() {
        return transformerFilters;
    }
    
    public void setTransformerFilters(List<ClassTransformerFilter> transformerFilters) {
        this.transformerFilters = transformerFilters;
    }
    
    public byte[] transform(ClassLoader loader, String className, Class<?> redefiningClass, 
    		ProtectionDomain domain, byte[] bytes) throws IllegalClassFormatException { 
    	logger.debug("Transforming class " + className + " ...");
        
        // Skip bootstrap classes
        if (loader == null) {
            return bytes;
        }
        
        // Skip java runtime classes
        for (String ignored : IGNORE) {
            if (className.startsWith(ignored)) {
                return bytes;
            }    
        }
        
        byte[] transformedBytes = bytes;
        
        try {
            ClassInfo ci = new ClassInfo(className, bytes, loader, redefiningClass, domain);
            
            logger.debug("Processing class " + ci.fullName() + " ...");
            
            for (ClassTransformerFilter ctf : transformerFilters) {
                transformedBytes = ctf.doFilter(ci);
                ci.setBytes(transformedBytes);
            }
        }
        catch (Throwable t) {
        	logger.error("Error at DefaultClassTransformer.transform()", t);
        } 
        
        return transformedBytes;
    }    
    
}
