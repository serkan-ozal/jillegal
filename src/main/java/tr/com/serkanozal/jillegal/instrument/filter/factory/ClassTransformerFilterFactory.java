/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.filter.factory;

import java.lang.annotation.Annotation;
import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jillegal.instrument.filter.ClassTransformerFilter;
import tr.com.serkanozal.jillegal.instrument.filter.impl.AnnotatedClassTransformerFilter;
import tr.com.serkanozal.jillegal.instrument.filter.impl.BaseClassTransformerFilter;
import tr.com.serkanozal.jillegal.instrument.filter.impl.RegExpClassTransformerFilter;

@SuppressWarnings( "static-access" )
public class ClassTransformerFilterFactory {
	
	protected static final Logger logger = Logger.getLogger(ClassTransformerFilterFactory.class);
	
	private static ClassLoader loader;
    
    static {
        loader = Thread.currentThread().getContextClassLoader().getSystemClassLoader();
    }
    
    @SuppressWarnings( "unchecked" )
    public static ClassTransformerFilter createClassTransformerFilter(String filterExp) {
        BaseClassTransformerFilter filter = null;
        
        filterExp = filterExp.trim();
        
        try {
            if ( filterExp.startsWith("annotated")) {
                String[] filterParts = filterExp.split(":");
                String annotationClsName = filterParts[1];
                String transformerClsName = filterParts[2];
                
                Class<? extends Annotation> annotationCls = 
                        (Class<? extends Annotation>) loader.loadClass(annotationClsName);
                Class<? extends ClassFileTransformer> transformerCls = 
                        (Class<? extends ClassFileTransformer>) loader.loadClass(transformerClsName);
                filter = new AnnotatedClassTransformerFilter(annotationCls, transformerCls.newInstance());
            }
            else if (filterExp.startsWith("regexp")) {
                String[] filterParts = filterExp.split(":");
                String regExp = filterParts[1];
                String transformerClsName = filterParts[2];
                
                Class<? extends ClassFileTransformer> transformerCls = 
                        (Class<? extends ClassFileTransformer>) loader.loadClass(transformerClsName);
                
                filter = new RegExpClassTransformerFilter(regExp, transformerCls.newInstance());
            }
            else if (filterExp.startsWith( "custom")) {
                String[] filterParts = filterExp.split(":");
                String customFilterClsName = filterParts[1];
                String transformerClsName = filterParts[2];
                
                Class<? extends ClassFileTransformer> transformerCls = 
                        (Class<? extends ClassFileTransformer>) loader.loadClass(transformerClsName);
                
                filter = (BaseClassTransformerFilter) loader.loadClass(customFilterClsName).newInstance();
                filter.setClassTransformer(transformerCls.newInstance());
            }
        }
        catch (Throwable t) {
            logger.error("Error at JavassistClassTransformerFilterFactory.createClassTransformerFilter()", t);
        }
        
        return filter;
    }
    
    public static List<ClassTransformerFilter> createClassTransformerFilters(String filterExp) {
        List<ClassTransformerFilter> transformerFilters = new ArrayList<ClassTransformerFilter>();
        
        for (String s : filterExp.split(";")) {
            if (s == null) {
                continue;
            }    
            s = s.trim();
            if (s.length() == 0) {
                continue;
            }
            ClassTransformerFilter tf = createClassTransformerFilter(s);
            if (tf != null) {
                transformerFilters.add( tf);
            }    
        }
        
        return transformerFilters;
    }
    
}
