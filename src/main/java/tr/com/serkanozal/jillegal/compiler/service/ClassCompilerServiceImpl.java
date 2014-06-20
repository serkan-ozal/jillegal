/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler.service;

import java.util.HashMap;
import java.util.Map;

import tr.com.serkanozal.jillegal.compiler.domain.model.CodeType;
import tr.com.serkanozal.jillegal.compiler.domain.model.DefaultCodeType;
import tr.com.serkanozal.jillegal.compiler.impl.ClassCompiler;
import tr.com.serkanozal.jillegal.compiler.impl.groovy.GroovyClassCompiler;
import tr.com.serkanozal.jillegal.compiler.impl.java.JavaClassCompiler;

public class ClassCompilerServiceImpl implements ClassCompilerService {

	protected Map<String, ClassCompiler> classCompilerMap = new HashMap<String, ClassCompiler>();
	
	public ClassCompilerServiceImpl() {
		initClassCompilerMap();
	}
	
	protected void initClassCompilerMap() {
		classCompilerMap.put(DefaultCodeType.JAVA.getTypeName(), new JavaClassCompiler());
		classCompilerMap.put(DefaultCodeType.GROOVY.getTypeName(), new GroovyClassCompiler());
	}
	
	protected void registerClassCompiler(String codeType, ClassCompiler classCompiler) {
		classCompilerMap.put(codeType, classCompiler);
	}

	@Override
	public void registerClassCompiler(CodeType codeType, ClassCompiler classCompiler) {
		registerClassCompiler(codeType.getTypeName(), classCompiler);
	}

	protected void unregisterClassCompiler(String codeType) {
		classCompilerMap.remove(codeType);
	}

	@Override
	public void unregisterClassCompiler(CodeType codeType) {
		unregisterClassCompiler(codeType.getTypeName());
	}
	
	@Override
	public ClassCompiler getClassCompiler(String codeType) {
		return classCompilerMap.get(codeType);
	}

	@Override
	public ClassCompiler getClassCompiler(CodeType codeType) {
		return getClassCompiler(codeType.getTypeName());
	}

}
