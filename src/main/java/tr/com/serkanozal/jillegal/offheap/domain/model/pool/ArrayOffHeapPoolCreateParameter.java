/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

public class ArrayOffHeapPoolCreateParameter<T> extends BaseOffHeapPoolCreateParameter<T> {

	protected int length;
	protected boolean usePrimitiveTypes;
	protected boolean initializeElements;
	
	public ArrayOffHeapPoolCreateParameter(Class<T> elementType, int length, boolean usePrimitiveTypes, 
			boolean initializeElements) {
		super(OffHeapPoolType.ARRAY_POOL, elementType);
		this.length = length;
		this.usePrimitiveTypes = usePrimitiveTypes;
		this.initializeElements = initializeElements;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	public boolean isUsePrimitiveTypes() {
		return usePrimitiveTypes;
	}
	
	public void setUsePrimitiveTypes(boolean usePrimitiveTypes) {
		this.usePrimitiveTypes = usePrimitiveTypes;
	}
	
	public boolean isInitializeElements() {
		return initializeElements;
	}
	
	public void setInitializeElements(boolean initializeElements) {
		this.initializeElements = initializeElements;
	}

}
