/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler.impl.groovy;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import tr.com.serkanozal.jillegal.compiler.exception.ClassCompileErrorBean;
import tr.com.serkanozal.jillegal.compiler.exception.ClassCompileException;
import tr.com.serkanozal.jillegal.compiler.impl.BaseClassCompiler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class GroovyClassCompiler extends BaseClassCompiler {

    private GroovyClassLoader gcl = new GroovyClassLoader();
	
	@Override
	public Class<?> compile(String code) throws ClassCompileException {
		try {
			return compile(new ByteArrayInputStream(code.getBytes("UTF8")));
		} 
		catch (UnsupportedEncodingException e) {
            logger.error("Error occurred while compiling class", e);
			throw new ClassCompileException(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<?> compile(InputStream codeInputStream) throws ClassCompileException {
		try {
			@SuppressWarnings("deprecation")
			Class<?> clazz = gcl.parseClass(codeInputStream);
			return clazz;
		}
		catch (MultipleCompilationErrorsException e) {
			List<Message> messages = e.getErrorCollector().getErrors();
			ClassCompileException classCompileException = new ClassCompileException();
			if (messages != null) {
				SyntaxException exception = null;
				for (Message msg : messages) {
					if (msg instanceof SyntaxErrorMessage) {
			            exception = ((SyntaxErrorMessage)msg).getCause();
			            classCompileException.addCompileError(
			            		new ClassCompileErrorBean(exception.getLine(), exception.getMessage()));
			        }
				}
			}
			throw classCompileException;
		}
		catch (CompilationFailedException e) {
			List<Message> messages = e.getUnit().getErrorCollector().getErrors();
			ClassCompileException classCompileException = new ClassCompileException();
			if (messages != null) {
				SyntaxException exception = null;
				for (Message msg : messages) {
					if (msg instanceof SyntaxErrorMessage) {
			            exception = ((SyntaxErrorMessage)msg).getCause();
			            classCompileException.addCompileError(
			            		new ClassCompileErrorBean(exception.getLine(), exception.getMessage()));
			        }
				}
			}
			throw classCompileException;
		}
	}

}
