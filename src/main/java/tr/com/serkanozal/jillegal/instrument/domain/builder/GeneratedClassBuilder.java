/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.domain.builder;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.instrument.domain.model.GeneratedClass;

public class GeneratedClassBuilder<T> implements Builder<GeneratedClass<T>> {

	private Class<T> sourceClass;
    private byte[] classData;
	
	@Override
	public GeneratedClass<T> build() {
		return new GeneratedClass<T>(sourceClass, classData);
	}
	
	public GeneratedClassBuilder<T> sourceClass(Class<T> sourceClass) {
		this.sourceClass = sourceClass;
		return this;
	}
	
	public GeneratedClassBuilder<T> classData(byte[] classData) {
		this.classData = classData;
		return this;
	}

}
