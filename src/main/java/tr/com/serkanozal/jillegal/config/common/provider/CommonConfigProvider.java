/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.config.common.provider;

import java.util.Set;

import tr.com.serkanozal.jillegal.config.ConfigProvider;

public interface CommonConfigProvider extends ConfigProvider {
	
	Set<Class<?>> getJillegalAwareClasses();
	
}
