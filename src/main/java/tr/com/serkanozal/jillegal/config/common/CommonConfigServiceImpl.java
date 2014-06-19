/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.config.common;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jillegal.config.BaseConfigService;
import tr.com.serkanozal.jillegal.config.common.provider.CommonConfigProvider;
import tr.com.serkanozal.jillegal.config.common.provider.annotation.AnnotationBasedCommonConfigProvider;

public class CommonConfigServiceImpl extends BaseConfigService<CommonConfigProvider> implements CommonConfigService {

	protected final Logger logger = Logger.getLogger(getClass());
	
	protected Set<Class<?>> jillegalAwareClasses;
	
	@Override
	protected void init() {
		registerConfigProvider(new AnnotationBasedCommonConfigProvider());
		
		findJillegalAwareClasses();
	}
	
	protected void findJillegalAwareClasses() {
		jillegalAwareClasses = new HashSet<Class<?>>();
		for (CommonConfigProvider configProvider : configProviders) {
			Set<Class<?>> jillegalAwareClassList = configProvider.getJillegalAwareClasses();
			if (jillegalAwareClassList != null) {
				jillegalAwareClasses.addAll(jillegalAwareClassList);
			}	
		}	
		logger.info("Found Jillegal-Aware classes: " + jillegalAwareClasses);
	}

	@Override
	public Set<Class<?>> getJillegalAwareClasses() {
		return jillegalAwareClasses;
	}
	
}
