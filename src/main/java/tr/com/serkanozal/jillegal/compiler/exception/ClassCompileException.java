/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler.exception;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ClassCompileException extends Exception {

	private List<ClassCompileErrorBean> compileErrors;
	
	public ClassCompileException() {
		
	}
	
	public ClassCompileException(String errorMessage) {
		super(errorMessage);
	}
	
	public ClassCompileException(List<ClassCompileErrorBean> compileErrors) {
		this.compileErrors = compileErrors;
	}
	
	public void addCompileError(ClassCompileErrorBean compileError) {
		if (compileErrors == null) {
			compileErrors = new ArrayList<ClassCompileErrorBean>();
		}
		compileErrors.add(compileError);
	}
	
	public List<ClassCompileErrorBean> getCompileErrors() {
		return compileErrors;
	}
	
	public void setCompileErrors(List<ClassCompileErrorBean> compileErrors) {
		this.compileErrors = compileErrors;
	}
	
	@Override
	public String getMessage() {
		return getErrorMessage();
	}
	
	@Override
	public String toString() {
		return getErrorMessage();
	}
	
	private String getErrorMessage() {
		StringBuffer sb = new StringBuffer();
		if (compileErrors != null) {
			for (ClassCompileErrorBean error : compileErrors) {
				sb.append(error.toString()).append("\n").append("\n");
			}
		}
		return sb.toString();
	}
	
}
