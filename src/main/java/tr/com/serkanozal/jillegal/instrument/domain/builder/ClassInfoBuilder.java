/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.domain.builder;

import java.security.ProtectionDomain;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.instrument.domain.model.ClassInfo;

public class ClassInfoBuilder implements Builder<ClassInfo> {

	private String classFullName;
	private byte[] bytes;
	private ClassLoader loader;
	private Class<?> redefiningClass;
	private ProtectionDomain domain;
	    
	@Override
	public ClassInfo build() {
		return new ClassInfo(classFullName, bytes, loader, redefiningClass, domain);
	}

	public ClassInfoBuilder classFullName(String classFullName) {
		this.classFullName = classFullName;
		return this;
	}

	public ClassInfoBuilder bytes(byte[] bytes) {
		this.bytes = bytes;
		return this;
	}

	public ClassInfoBuilder loader(ClassLoader loader) {
		this.loader = loader;
		return this;
	}

	public ClassInfoBuilder redefiningClass(Class<?> redefiningClass) {
		this.redefiningClass = redefiningClass;
		return this;
	}

	public ClassInfoBuilder domain(ProtectionDomain domain) {
		this.domain = domain;
		return this;
	}

}
