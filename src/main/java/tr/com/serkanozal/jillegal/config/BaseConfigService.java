/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.config;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseConfigService<P extends ConfigProvider> implements ConfigService<P> {

	protected Set<P> configProviders = new HashSet<P>();
	
	protected BaseConfigService() {
		init();
	}
	
	protected void init() {
		
	}

	@Override
	public void registerConfigProvider(P configProvider) {
		configProviders.add(configProvider);
	}

	@Override
	public void unregisterConfigProvider(P configProvider) {
		configProviders.remove(configProvider);
	}

	@Override
	public Set<P> getConfigProviders() {
		return configProviders;
	}
	
}
