/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.instance;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.instance.ArrayInstanceRequest;

public class ArrayInstanceRequestBuilder<T> implements Builder<ArrayInstanceRequest<T>> {

	private Class<T> arrayType;
	private int length;
	private boolean initializeElements;
	
	@Override
	public ArrayInstanceRequest<T> build() {
		return new ArrayInstanceRequest<T>(arrayType, length, initializeElements);
	}
	
	public ArrayInstanceRequestBuilder<T> arrayType(Class<T> arrayType) {
		this.arrayType = arrayType;
		return this;
	}
	
	public ArrayInstanceRequestBuilder<T> length(int length) {
		this.length = length;
		return this;
	}
	
	public ArrayInstanceRequestBuilder<T> initializeElements(boolean initializeElements) {
		this.initializeElements = initializeElements;
		return this;
	}

}
