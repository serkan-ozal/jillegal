/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.config;

import java.util.HashMap;
import java.util.Map;

import tr.com.serkanozal.jillegal.config.BaseConfigService;
import tr.com.serkanozal.jillegal.offheap.config.provider.OffHeapConfigProvider;
import tr.com.serkanozal.jillegal.offheap.config.provider.annotation.AnnotationBasedOffHeapConfigProvider;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapClassConfig;

public class OffHeapConfigServiceImpl extends BaseConfigService<OffHeapConfigProvider> implements OffHeapConfigService {

	protected Map<Class<?>, OffHeapClassConfig> classConfigMap = new HashMap<Class<?>, OffHeapClassConfig>();
	
	@Override
	protected void init() {
		registerConfigProvider(new AnnotationBasedOffHeapConfigProvider());
	}
	
	@Override
	public OffHeapClassConfig getOffHeapClassConfig(Class<?> clazz) {
		OffHeapClassConfig classConfig = classConfigMap.get(clazz);
		if (classConfig == null) {
			classConfig = findOffHeapClassConfig(clazz);
			classConfigMap.put(clazz, classConfig);
		}
		return classConfig;
	}
	
	protected OffHeapClassConfig findOffHeapClassConfig(Class<?> clazz) {
		OffHeapClassConfig classConfig = null;
		for (OffHeapConfigProvider configProvider : configProviders) {
			if (classConfig == null) {
				classConfig = configProvider.getOffHeapClassConfig(clazz);
			}
			else {
				OffHeapClassConfig config = configProvider.getOffHeapClassConfig(clazz);
				if (config != null) {
					classConfig = classConfig.merge(config);
				}
			}
		}
		return classConfig;
	}
	
}
