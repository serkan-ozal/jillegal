/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.config;

public class OffHeapArrayFieldConfig extends OffHeapFieldConfig {
	
	protected Class<?> elementType;
	protected int length;
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	public Class<?> getElementType() {
		return elementType;
	}
	
	public void setElementType(Class<?> elementType) {
		this.elementType = elementType;
	}
	
}
