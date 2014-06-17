/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.config;

public class OffHeapObjectFieldConfig extends OffHeapFieldConfig {
	
	protected Class<?> fieldType;
	
	public Class<?> getFieldType() {
		return fieldType;
	}
	
	public void setFieldType(Class<?> fieldType) {
		this.fieldType = fieldType;
	}
	
}
