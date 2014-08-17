/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.instance;

public class ArrayInstanceRequest<T> implements InstanceRequest<T> {

	private Class<T> arrayType;
	private int length;
	
	public ArrayInstanceRequest() {

	}
	
	public ArrayInstanceRequest(Class<T> arrayType, int length) {
		this.arrayType = arrayType;
		this.length = length;
	}

	public Class<T> getArrayType() {
		return arrayType;
	}
	
	public void setArrayType(Class<T> arrayType) {
		this.arrayType = arrayType;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	@Override
	public Class<T> getInstanceType() {
		return arrayType;
	}
	
}
