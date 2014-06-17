/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.config;

import tr.com.serkanozal.jillegal.config.ConfigService;
import tr.com.serkanozal.jillegal.offheap.config.provider.OffHeapConfigProvider;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapClassConfig;

public interface OffHeapConfigService extends ConfigService<OffHeapConfigProvider> {
	
	OffHeapClassConfig getOffHeapClassConfig(Class<?> clazz);
	
}
