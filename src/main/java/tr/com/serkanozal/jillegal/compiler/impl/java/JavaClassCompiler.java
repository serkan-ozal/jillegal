/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler.impl.java;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

import tr.com.serkanozal.jillegal.compiler.exception.ClassCompileErrorBean;
import tr.com.serkanozal.jillegal.compiler.exception.ClassCompileException;
import tr.com.serkanozal.jillegal.compiler.impl.BaseClassCompiler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaClassCompiler extends BaseClassCompiler {

    private static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile("package[\\s]+([^;]+)");
	private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("class[\\s]+([^\\s]+)");
	
	public JavaClassCompiler() {
		
	}

	@Override
	public Class<?> compile(InputStream codeInputStream) throws ClassCompileException {
		return compile(new Scanner(codeInputStream).useDelimiter("\\A").next());
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Class<?> compile(String code) throws ClassCompileException {
		Matcher m1 = CLASS_NAME_PATTERN.matcher(code);
		Matcher m2 = PACKAGE_NAME_PATTERN.matcher(code);
		m1.find();
		m2.find();
		String className = m1.group(1);
		String packageName = m2.group(1);
		String generatedClassName = className;// + "$" + System.nanoTime();
		
		String fullName = packageName + "." + generatedClassName;
		code = code.replace(className, generatedClassName);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		JavaFileManager fileManager = 
				new JavaClassFileManager(compiler.getStandardFileManager(null, null, null), fullName);
		List<JavaFileObject> jObjects = new ArrayList<JavaFileObject>();
		jObjects.add(new CharSequenceJavaFileObject(fullName, code));
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		List<String> options = new ArrayList<String>();
		options.add("-cp");
		options.add(findClassPath());
		
        CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, jObjects);
        boolean success = task.call();
		if (success) {
			try {
				Class<?> clazz = fileManager.getClassLoader(null).loadClass(fullName);
				return clazz;
			}
			catch (Exception e) {
                logger.error("Error occurred while compiling class", e);
				throw new ClassCompileException(e.getMessage());
			} 
		}	
		else {
			ClassCompileException classCompileException = new ClassCompileException();
			for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
				classCompileException.addCompileError(
	            	new ClassCompileErrorBean(diagnostic.getLineNumber(), diagnostic.getMessage(null)));
			}
			throw classCompileException;   
		}
	}
	
}
