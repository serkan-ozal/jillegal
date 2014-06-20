/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler.exception;

public class ClassCompileErrorBean {

	private long lineNo;
	private String errorMessage;
	
	public ClassCompileErrorBean() {
		
	}
	
	public ClassCompileErrorBean(long lineNo, String errorMessage) {
		this.lineNo = lineNo;
		this.errorMessage = errorMessage;
	}
	
	public long getLineNo() {
		return lineNo;
	}
	
	public void setLineNo(long lineNo) {
		this.lineNo = lineNo;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return "Line Number: " + lineNo + "\n" + "Error Message: " + errorMessage;
	}
	
}
