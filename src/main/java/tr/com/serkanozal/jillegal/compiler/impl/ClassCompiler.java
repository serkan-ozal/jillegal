/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler.impl;

import java.io.InputStream;

import tr.com.serkanozal.jillegal.compiler.domain.model.DefaultCodeType;
import tr.com.serkanozal.jillegal.compiler.exception.ClassCompileException;

public interface ClassCompiler {

	DefaultCodeType DEFAULT_CLASS_TYPE = DefaultCodeType.JAVA;
	
	Class<?> compile(String code) throws ClassCompileException;
	Class<?> compile(InputStream codeInputStream) throws ClassCompileException;
	
}
