/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler.impl;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jillegal.util.ClasspathUtil;

public abstract class BaseClassCompiler implements ClassCompiler {

	protected final Logger logger = Logger.getLogger(getClass());
	
	protected String findClassPath() {
		return ClasspathUtil.getFullClasspath();
	}
	
}
