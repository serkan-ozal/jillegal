/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.filter.impl;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;

import tr.com.serkanozal.jillegal.instrument.domain.model.ClassInfo;

import javassist.ClassPool;
import javassist.CtClass;

public abstract class BaseClassTransformerFilter extends AbstractClassTransformerFilter {
	
    protected ClassPool cp = ClassPool.getDefault();
    
    public BaseClassTransformerFilter() {

    }
    
    public BaseClassTransformerFilter(ClassFileTransformer classTransformer) {
        super(classTransformer);
    }
    
    public ClassFileTransformer getClassTransformer() {
        return classTransformer;
    }
    
    public void setClassTransformer(ClassFileTransformer classTransformer) {
        this.classTransformer = classTransformer;
    }
    
    @Override
    public byte[] doFilter(ClassInfo ci) {
        try {
        	CtClass cc = null;
        	try {
        		cc = cp.get(ci.getClassFullName().replace("/", "."));
        		cc.defrost();
        	}
        	catch (Exception e) {
        		cc = cp.makeClass(new ByteArrayInputStream(ci.getBytes()));
        	}
            
            if (useFilter(cc)) {
                if (classTransformer != null) {
                    return classTransformer.transform(ci.getLoader(), ci.getClassFullName(), 
                            		ci.getRedefiningClass(), ci.getDomain(), ci.getBytes());
                }
                else {
                	return executeFilter(ci);
                }
            }
        }
        catch (Throwable t) {
            logger.error("Error at BaseClassTransformerFilter.doFilter()", t);
        }
        
        return ci.getBytes();
    }
    
    protected byte[] executeFilter(ClassInfo ci) {
    	return ci.getBytes();
    }
    
    abstract protected boolean useFilter(CtClass cls);
    
}
