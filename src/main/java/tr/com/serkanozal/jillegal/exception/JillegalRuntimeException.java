/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.exception;

@SuppressWarnings("serial")
public class JillegalRuntimeException extends RuntimeException {

	public JillegalRuntimeException() {
		
	}
	
	public JillegalRuntimeException(String msg) {
		super(msg);
	}
	
	public JillegalRuntimeException(Throwable cause) {
		super(cause);
	}
	
	public JillegalRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
