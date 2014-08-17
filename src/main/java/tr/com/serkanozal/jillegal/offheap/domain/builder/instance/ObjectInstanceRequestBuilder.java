/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.instance;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.instance.ObjectInstanceRequest;

public class ObjectInstanceRequestBuilder<T> implements Builder<ObjectInstanceRequest<T>> {

	private Class<T> objectType;
	
	@Override
	public ObjectInstanceRequest<T> build() {
		return new ObjectInstanceRequest<T>(objectType);
	}
	
	public ObjectInstanceRequestBuilder<T> objectType(Class<T> objectType) {
		this.objectType = objectType;
		return this;
	}

}
