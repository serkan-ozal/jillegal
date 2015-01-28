/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.config;

import java.lang.reflect.Field;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapArrayFieldConfig;

public class OffHeapArrayFieldConfigBuilder implements Builder<OffHeapArrayFieldConfig> {

	private Field field;
	private Class<?> elementType;
	private int length;
	private boolean initializeElements;
	
	public OffHeapArrayFieldConfigBuilder field(Field field) {
		this.field = field;
		return this;
	}
	
	public OffHeapArrayFieldConfigBuilder elementType(Class<?> elementType) {
		if (!elementType.equals(Object.class)) {
			this.elementType = elementType;
		}	
		return this;
	}
	
	public OffHeapArrayFieldConfigBuilder length(int length) {
		this.length = length;
		return this;
	}
	
	public OffHeapArrayFieldConfigBuilder initializeElements(boolean initializeElements) {
		this.initializeElements = initializeElements;
		return this;
	}
	
	@Override
	public OffHeapArrayFieldConfig build() {
		OffHeapArrayFieldConfig config = new OffHeapArrayFieldConfig();
		config.setField(field);
		config.setElementType(elementType);
		config.setLength(length);
		config.setInitializeElements(initializeElements);
		return config;
	}
	
}
