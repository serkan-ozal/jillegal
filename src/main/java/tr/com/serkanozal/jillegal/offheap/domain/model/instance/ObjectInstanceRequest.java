/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.instance;

public class ObjectInstanceRequest<T> implements InstanceRequest<T> {

	private Class<T> objectType;
	
	public ObjectInstanceRequest() {
		
	}
	
	public ObjectInstanceRequest(Class<T> objectType) {
		this.objectType = objectType;
	}
	
	public Class<T> getObjectType() {
		return objectType;
	}
	
	public void setObjectType(Class<T> objectType) {
		this.objectType = objectType;
	}
	
	@Override
	public Class<T> getInstanceType() {
		return objectType;
	}
	
}
