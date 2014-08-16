/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.exception;

import tr.com.serkanozal.jillegal.exception.JillegalRuntimeException;

@SuppressWarnings("serial")
public class NoAvailableObjectException extends JillegalRuntimeException {

	public NoAvailableObjectException() {
		super("There is no available object !");
	}
	
	public NoAvailableObjectException(String msg) {
		super(msg);
	}
	
}
