/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.config.common.provider.annotation;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jillegal.config.common.provider.CommonConfigProvider;
import tr.com.serkanozal.jillegal.scanner.JillegalAwareScanner;
import tr.com.serkanozal.jillegal.scanner.JillegalAwareScannerFactory;

public class AnnotationBasedCommonConfigProvider implements CommonConfigProvider {

	protected final Logger logger = Logger.getLogger(getClass());
	
	protected final JillegalAwareScanner jillegalAwareScanner = 
			JillegalAwareScannerFactory.getJillegalAwareScanner();
	protected Set<Class<?>> jillegalAwareClasses;
	
	public AnnotationBasedCommonConfigProvider() {
		init();
	}
	
	protected void init() {
		scanJillegalAwareClasses();
	}
	
	protected void scanJillegalAwareClasses() {
		logger.info("Scanning started for Jillegal-Aware classes ..."); 
		long start = System.currentTimeMillis();
		jillegalAwareClasses = 
				new HashSet<Class<?>>(
						jillegalAwareScanner.getJillegalAwareClasses());
		long finish = System.currentTimeMillis();
		logger.info("Scanning finished for Jillegal-Aware classes in " + (finish - start) + " milliseconds"); 
	}
	
	@Override
	public Set<Class<?>> getJillegalAwareClasses() {
		return jillegalAwareClasses;
	}
	
}
