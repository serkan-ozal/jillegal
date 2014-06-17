/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.config;

import java.lang.reflect.Field;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapObjectFieldConfig;

public class OffHeapObjectFieldConfigBuilder implements Builder<OffHeapObjectFieldConfig> {

	private Field field;
	private Class<?> fieldType;
	
	public OffHeapObjectFieldConfigBuilder field(Field field) {
		this.field = field;
		return this;
	}
	
	public OffHeapObjectFieldConfigBuilder fieldType(Class<?> fieldType) {
		if (!fieldType.equals(Object.class)) {
			this.fieldType = fieldType;
		}	
		return this;
	}
	
	@Override
	public OffHeapObjectFieldConfig build() {
		OffHeapObjectFieldConfig config = new OffHeapObjectFieldConfig();
		config.setField(field);
		config.setFieldType(fieldType);
		return config;
	}
	
}
