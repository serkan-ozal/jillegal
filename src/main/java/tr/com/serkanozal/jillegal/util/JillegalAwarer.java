/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util;

import java.util.Set;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jillegal.config.common.CommonConfigService;
import tr.com.serkanozal.jillegal.config.common.CommonConfigServiceFactory;
import tr.com.serkanozal.jillegal.instrument.transformer.impl.JillegalAwareClassTransformer;
import tr.com.serkanozal.jillegal.offheap.util.OffHeapAwarer;

public class JillegalAwarer {
	
	private static final Logger logger = Logger.getLogger(JillegalUtil.class);
	
	private static final CommonConfigService commonConfigService = CommonConfigServiceFactory.getCommonConfigService();
	private static final JillegalAwareClassTransformer jillegalAwareClassTransformer = new JillegalAwareClassTransformer();
	
	private static boolean awared = false;
	
	private JillegalAwarer() {
        
    }
	
	public synchronized static void makeJillegalAware() {
		if (!awared) {
			Set<Class<?>> jillegalAwareClasses = commonConfigService.getJillegalAwareClasses();
			if (jillegalAwareClasses != null) {
				for (Class<?> jillegalAwareClass : jillegalAwareClasses) {
					try {
						logger.error("Class " + jillegalAwareClass.getName() + " will be maked Jillegal-Aware ...");
						jillegalAwareClassTransformer.transform(jillegalAwareClass);
						logger.error("Class " + jillegalAwareClass.getName() + " has been maked Jillegal-Aware ...");
					}
					catch (Throwable t) {
						logger.error("Unable to making class " + jillegalAwareClass.getName() + " Jillegal-Aware", t);
					}
				}
			}
			awared = true;
		}	
	}
	
	public static <T> void doJillegalAware(T obj) {
		OffHeapAwarer.doOffHeapAware(obj);
	}
    
}
