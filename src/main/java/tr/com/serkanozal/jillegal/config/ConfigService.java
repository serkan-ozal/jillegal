/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.config;

import java.util.Set;

public interface ConfigService<P extends ConfigProvider> {

	void registerConfigProvider(P configProvider);
	void unregisterConfigProvider(P configProvider);
	Set<P> getConfigProviders();
	
}
