/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.domain.model;

import java.security.ProtectionDomain;

public class ClassInfo {
	
    private String packageName  = "";
    private String className    = "";
    private String classFullName;
    private byte[] bytes;
    private ClassLoader loader;
    private Class<?> redefiningClass;
    private ProtectionDomain domain;
    
    public ClassInfo() {
        
    }
  
    public ClassInfo(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
    }
    
    public ClassInfo(String classFullName, byte[] bytes, ClassLoader loader, Class<?>  redefiningClass, ProtectionDomain domain) {
        this.classFullName = classFullName;
        classFullName = classFullName.replace("/", ".");
        int lastSeparatorIndex  = classFullName.lastIndexOf(".");
        
        if (lastSeparatorIndex > 0) {
            packageName = classFullName.substring(0, lastSeparatorIndex);
            className = classFullName.substring(lastSeparatorIndex + 1);
        }
        else {
            packageName = "";
            className = classFullName;
        }
        
        this.bytes = bytes;
        this.loader = loader;
        this.redefiningClass = redefiningClass;
        this.domain = domain;
    }

    public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassFullName() {
		return classFullName;
	}

	public void setClassFullName(String classFullName) {
		this.classFullName = classFullName;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public ClassLoader getLoader() {
		return loader;
	}

	public void setLoader(ClassLoader loader) {
		this.loader = loader;
	}

	public Class<?> getRedefiningClass() {
		return redefiningClass;
	}

	public void setRedefiningClass(Class<?> redefiningClass) {
		this.redefiningClass = redefiningClass;
	}

	public ProtectionDomain getDomain() {
		return domain;
	}

	public void setDomain(ProtectionDomain domain) {
		this.domain = domain;
	}

	public String fullName() {
        if (packageName != null && packageName.length() > 0) {
            return packageName + "." + className;
        }    
        else {
            return className;
        }    
    }
    
    @Override
    public String toString() {
        return fullName();
    }
    
}
