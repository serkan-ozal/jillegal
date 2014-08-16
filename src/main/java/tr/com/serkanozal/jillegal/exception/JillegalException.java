/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.exception;

@SuppressWarnings("serial")
public class JillegalException extends Exception {

	public JillegalException() {
		
	}
	
	public JillegalException(String msg) {
		super(msg);
	}
	
	public JillegalException(Throwable cause) {
		super(cause);
	}
	
	public JillegalException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
