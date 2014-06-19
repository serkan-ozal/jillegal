/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.config.common;

import java.util.Set;

import tr.com.serkanozal.jillegal.config.ConfigService;
import tr.com.serkanozal.jillegal.config.common.provider.CommonConfigProvider;

public interface CommonConfigService extends ConfigService<CommonConfigProvider> {
	
	Set<Class<?>> getJillegalAwareClasses();
	
}
