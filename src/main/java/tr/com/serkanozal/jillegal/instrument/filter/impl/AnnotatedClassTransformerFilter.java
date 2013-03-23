/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.filter.impl;

import java.lang.annotation.Annotation;
import java.lang.instrument.ClassFileTransformer;

import javassist.CtClass;

public class AnnotatedClassTransformerFilter extends BaseClassTransformerFilter {
	
    private Class<? extends Annotation> annotationClass;

    public AnnotatedClassTransformerFilter(Class<? extends Annotation> annotationClass, ClassFileTransformer classTransformer) {
        super(classTransformer);
        this.annotationClass = annotationClass;
    }
    
    @SuppressWarnings("unchecked")
    public AnnotatedClassTransformerFilter(String annotationClassName, ClassFileTransformer classTransformer) {
        super(classTransformer);
        try {
            this.annotationClass = (Class<? extends Annotation>) getClass().getClassLoader().loadClass(annotationClassName);
        }
        catch (ClassNotFoundException e) {
            logger.error("Error at AnnotatedClassTransformerFilter.AnnotatedClassTransformerFilter()", e);
        }
    }
    
    public Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }
    
    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }
    
    @Override
    public boolean useFilter(CtClass cc) {
        try {
            for (Object annotation : cc.getAnnotations()) {    
                String annotationClsName = annotation.toString().substring(1);
                if (annotationClsName.equals(annotationClass.getName())) {
                    return true;
                }    
            }    
        }
        catch (Throwable t) {
        	logger.error("Error at AnnotatedClassTransformerFilter.useFilter()", t);
        }
        
        return false;
    }

}
