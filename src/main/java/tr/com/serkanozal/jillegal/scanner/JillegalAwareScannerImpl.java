/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.scanner;

import java.util.Set;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import tr.com.serkanozal.jillegal.config.annotation.JillegalAware;
import tr.com.serkanozal.jillegal.util.ClasspathUtil;

public class JillegalAwareScannerImpl implements JillegalAwareScanner {
	
	private Reflections reflections = 
			new Reflections(
					new ConfigurationBuilder().
							setUrls(ClasspathUtil.getClasspathUrls()));

	private Set<Class<?>> jillegalAwareClasses;
	
	@Override
	public synchronized Set<Class<?>> getJillegalAwareClasses() {
		if (jillegalAwareClasses == null) {
			jillegalAwareClasses = 
					reflections.getTypesAnnotatedWith(JillegalAware.class);
		}
		return jillegalAwareClasses;
	}
	
}
