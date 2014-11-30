/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util.compressedoops.hotspot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import sun.misc.Resource;

public class HotspotJvmAwareSaJdiClassLoader extends URLClassLoader {

	public static final String URL_CLASSPATH_VM_ARGUMENT_NAME = "url.classpath";
	
	private static final String SA_JDI_PACKAGE_PREFIX = "sun/jvm/hotspot";
	private static final String SA_JDI_PROPERTIES_FILE_NAME = "sa.properties";
	private static final String SA_JDI_JVM_PREFIX;
	
	static {
		String javaSpecVersion = System.getProperty("java.specification.version");
		if (javaSpecVersion.equals("1.6")) {
			SA_JDI_JVM_PREFIX = "java6";
		}
		else if (javaSpecVersion.equals("1.7")) {
			SA_JDI_JVM_PREFIX = "java7";
		}
		else if (javaSpecVersion.equals("1.8")) {
			SA_JDI_JVM_PREFIX = "java8";
		}
		else {
			throw new IllegalStateException("Unsupported Java version: " + javaSpecVersion);
		}
	}
	
	public HotspotJvmAwareSaJdiClassLoader() {
        super(findClasspathURLs());
        try {
			Field urlClasspathField = URLClassLoader.class.getDeclaredField("ucp");
			urlClasspathField.setAccessible(true);
			urlClasspathField.set(
					this, 
					new JvmAwareUrlClasspath(findClasspathURLs()));
		} 
        catch (Throwable t) {
			throw new IllegalStateException(t);
		} 
	}

	public HotspotJvmAwareSaJdiClassLoader(ClassLoader parent) {
        super(findClasspathURLs(), parent);
        try {
			Field urlClasspathField = URLClassLoader.class.getDeclaredField("ucp");
			urlClasspathField.setAccessible(true);
			urlClasspathField.set(
					this, 
					new JvmAwareUrlClasspath(findClasspathURLs()));
			if (parent instanceof URLClassLoader) {
				urlClasspathField.set(
					parent, 
					new JvmAwareUrlClasspath(findClasspathURLs()));
			}
		} 
        catch (Throwable t) {
			throw new IllegalStateException(t);
		} 
	}
	
	private static URL[] findClasspathURLs() {
		String urlClasspath = System.getProperty(URL_CLASSPATH_VM_ARGUMENT_NAME);
		if (urlClasspath.startsWith("[")) {
			urlClasspath = urlClasspath.substring(1);
		}
		if (urlClasspath.endsWith("]")) {
			urlClasspath = urlClasspath.substring(0, urlClasspath.length() - 1);
		}
		String[] urlClasspathUrls = urlClasspath.split("," + " | " + File.pathSeparator);
		URL[] urls = new URL[urlClasspathUrls.length];
		for (int i = 0; i < urlClasspathUrls.length; i++) {
			try {
				urls[i] = new URL(urlClasspathUrls[i].trim().replace("%20", " "));
			} 
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return urls;
	}

	public static class JvmAwareUrlClasspath extends sun.misc.URLClassPath {

		public JvmAwareUrlClasspath(URL[] urls) {
			super(urls);
		}
		
		@Override
		public URL findResource(String name, boolean check) {
			if (	name.equals(SA_JDI_PROPERTIES_FILE_NAME) || 
					name.startsWith(SA_JDI_PACKAGE_PREFIX)) {
				name = SA_JDI_JVM_PREFIX + "/" + name;
			}
			return super.findResource(name, check);
		}
		
		@Override
		public Resource getResource(String name) {
			final String originalName = name;
			if (	name.equals(SA_JDI_PROPERTIES_FILE_NAME) || 
					name.startsWith(SA_JDI_PACKAGE_PREFIX)) {
				name = SA_JDI_JVM_PREFIX + "/" + name;
			}
			return new DelegatedResource(originalName, super.getResource(name));
		}
		
		@Override
		public Resource getResource(String name, boolean check) {
			final String originalName = name;
			if (	name.equals(SA_JDI_PROPERTIES_FILE_NAME) || 
					name.startsWith(SA_JDI_PACKAGE_PREFIX)) {
				name = SA_JDI_JVM_PREFIX + "/" + name;
			}
			return new DelegatedResource(originalName, super.getResource(name, check));
		}

	}
	
	private static class DelegatedResource extends sun.misc.Resource {
		
		private String resourceName;
		private sun.misc.Resource delegatedResource;
		
		DelegatedResource(String resourceName, sun.misc.Resource delegatedResource) {
			this.resourceName = resourceName;
			this.delegatedResource = delegatedResource;
		}
		
		public String getName() { 
        	return resourceName; 
        }
        
        public URL getURL() { 
        	return delegatedResource == null ? null : delegatedResource.getURL(); 
        }
        
        public URL getCodeSourceURL() { 
        	return delegatedResource == null ? null : delegatedResource.getCodeSourceURL(); 
        }
        
        public InputStream getInputStream() throws IOException {
            return delegatedResource == null ? null : delegatedResource.getInputStream();
        }
        
        public int getContentLength() throws IOException {
            return delegatedResource == null ? null : delegatedResource.getContentLength();
        }
		
	}

}
