/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.filter.impl;

import java.lang.instrument.ClassFileTransformer;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jillegal.instrument.filter.ClassTransformerFilter;

public abstract class AbstractClassTransformerFilter implements ClassTransformerFilter {

	protected final Logger logger = Logger.getLogger(getClass());
	
	protected ClassFileTransformer classTransformer;
	
	public AbstractClassTransformerFilter() {

	}
	
	public AbstractClassTransformerFilter(ClassFileTransformer classTransformer) {
		this.classTransformer = classTransformer;
	}
	
}
