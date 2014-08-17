/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.instance;

public class StringInstanceRequest implements InstanceRequest<String> {

	private String str;
	
	public StringInstanceRequest() {
	
	}
	
	public StringInstanceRequest(String str) {
		this.str = str;
	}

	public String getString() {
		return str;
	}
	
	public void setString(String str) {
		this.str = str;
	}
	
	@Override
	public Class<String> getInstanceType() {
		return String.class;
	}
	
}
