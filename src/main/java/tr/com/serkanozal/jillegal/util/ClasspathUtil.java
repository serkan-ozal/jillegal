/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class ClasspathUtil {
	
	private static final Logger logger = Logger.getLogger(ClasspathUtil.class);
	
	private static Set<URL> classpathUrls;
	private static String fullClasspath;
	
	static {
		init();
	}
	
	private ClasspathUtil() {
		
	}
	
	public static Set<URL> getClasspathUrls() {
		return classpathUrls;
	}
	
	public static String getFullClasspath() {
		return fullClasspath;
	}
	
	private static void init() {
		classpathUrls = findClasspathUrls();
		logger.info("Found classpath URL list: " + classpathUrls);
		StringBuilder classpathBuilder = new StringBuilder();
		if (classpathUrls != null) {
			for (URL url : classpathUrls) {
				classpathBuilder.append(url.getPath()).append(File.pathSeparator);
			}
		}
		fullClasspath = classpathBuilder.toString();
	}
	
	private static Set<URL> findClasspathUrls() {
		Set<URL> urls = new HashSet<URL>();
		
		try {
			String[] classpathProperties = System.getProperty("java.class.path").split(File.pathSeparator);
			for (String classpathProperty : classpathProperties) {
				urls.add(new File(classpathProperty).toURI().toURL());
			}	
		} 
		catch (MalformedURLException e) {
			logger.error("Error occured while getting classpath from system property \"java.class.path\"", e);
		}
		
		String surefireProperty = System.getProperty("surefire.test.class.path");
		if (StringUtils.isNotEmpty(surefireProperty)) {
			try {
				String[] surefireClasspathProperties = surefireProperty.split(File.pathSeparator);
				for (String surefireClasspathProperty : surefireClasspathProperties) {
					urls.add(new File(surefireClasspathProperty).toURI().toURL());
				}	
			} 
			catch (MalformedURLException e) {
				logger.error("Error occured while getting classpath from system property \"surefire.test.class.path\"", e);
			}
		}
		
		// Start with Current Thread's loader
		ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
		ClassLoader loader = ctxLoader;
		while (loader != null) {
			urls.addAll(findClasspathsByLoader(loader));
			loader = loader.getParent();
		}

		// Also start with this classes's loader, in some environment this can
		// be different than the current thread's one
		ClassLoader appLoader = ClasspathUtil.class.getClassLoader();
		loader = appLoader;
		while (loader != null) {
			urls.addAll(findClasspathsByLoader(loader));
			loader = loader.getParent();
		}
		
		ClassLoader sysLoader = ClassLoader.getSystemClassLoader();
		loader = sysLoader;
		while (loader != null) {
			urls.addAll(findClasspathsByLoader(loader));
			loader = loader.getParent();
		}
		
		Map<URL, URL> replaceURLs = new HashMap<URL, URL>();
		Set<URL> derivedUrls = new HashSet<URL>();
		for (URL url : urls) {
			if (url.getProtocol().startsWith("vfs")) {
				try {
					URLConnection conn = url.openConnection();
					Object virtualFile = conn.getContent();
					if (virtualFile.getClass().getName().equals("org.jboss.vfs.VirtualFile")) {
						File file = 
								(File) virtualFile.getClass().
											getMethod("getPhysicalFile").
												invoke(virtualFile);
						String fileName = file.getCanonicalPath();
						String name = 
								(String) virtualFile.getClass().
											getMethod("getName").
												invoke(virtualFile);
						name = name.trim().toLowerCase();
						if (	(name.endsWith("jar") || 
								name.endsWith("zip") && 
								fileName.endsWith("/contents"))) {
							fileName = fileName.replace("contents", name);
						}
						URL repURL = new URL("file:/" + fileName);
						replaceURLs.put(url, repURL);
					}
				} 
				catch (Exception e) {
					// We don't expect to trapped here
					e.printStackTrace();
				}
			}
			try {
				if (url.toExternalForm().endsWith("WEB-INF/classes")) {
					derivedUrls.add(
							new URL(
									url.toExternalForm().
										replace("WEB-INF/classes", "WEB-INF/lib")));
				} 
				else if (url.toExternalForm().endsWith("WEB-INF/classes/")) {
					derivedUrls.add(
							new URL(
									url.toExternalForm().
										replace("WEB-INF/classes/", "WEB-INF/lib/")));
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		urls.removeAll(replaceURLs.keySet());
		urls.addAll(replaceURLs.values());
		urls.addAll(derivedUrls);
		replaceURLs.clear();
		//Check contained urls
		for (URL url : urls) {
			for (URL rootUrl : urls) {
				if (url.equals(rootUrl)) {
					continue;
				}
				if (url.toExternalForm().startsWith(rootUrl.toExternalForm())) {
					if (replaceURLs.get(url) != null) {
						URL settledUrl =replaceURLs.get(url);
						if (settledUrl.toExternalForm().startsWith(rootUrl.toExternalForm())) {
							replaceURLs.put(url, rootUrl);	
						}
					}
					else {
						replaceURLs.put(url, rootUrl);						
					}
				}
			}
		}
		urls.removeAll(replaceURLs.keySet());
		return urls;
	}

	
	private static Set<URL> findClasspathsByLoader(ClassLoader loader) {
		Set<URL> urls = new HashSet<URL>();
		if (loader instanceof URLClassLoader) {
			URLClassLoader urlLoader = (URLClassLoader) loader;
			urls.addAll(Arrays.asList(urlLoader.getURLs()));
		} 
		else {
			Enumeration<URL> urlEnum;
			try {
				urlEnum = loader.getResources("");
				while (urlEnum.hasMoreElements()) {
					URL url = urlEnum.nextElement();
					if (url.getProtocol().startsWith("bundleresource")){
						continue;
					}
					urls.add(url);
				}
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return urls;
	}
	
}
