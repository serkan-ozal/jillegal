/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.exception;

import tr.com.serkanozal.jillegal.exception.JillegalRuntimeException;

@SuppressWarnings("serial")
public class ObjectInUseException extends JillegalRuntimeException {

	public ObjectInUseException() {
		super("Requested object is already in use !");
	}
	
	public ObjectInUseException(int index) {
		super("Requested object at index " + index + " is already in use !");
	}
	
	public ObjectInUseException(String msg) {
		super(msg);
	}
	
}
