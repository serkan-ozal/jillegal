/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.config.provider;

import tr.com.serkanozal.jillegal.config.ConfigProvider;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapClassConfig;

public interface OffHeapConfigProvider extends ConfigProvider {

	OffHeapClassConfig getOffHeapClassConfig(Class<?> clazz);
	
}
