/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler.service;

import tr.com.serkanozal.jillegal.compiler.domain.model.CodeType;
import tr.com.serkanozal.jillegal.compiler.impl.ClassCompiler;

public interface ClassCompilerService {

	void registerClassCompiler(CodeType codeType, ClassCompiler classCompiler);
	void unregisterClassCompiler(CodeType codeType);
	
	ClassCompiler getClassCompiler(String codeType);
	ClassCompiler getClassCompiler(CodeType codeType);
	
}
