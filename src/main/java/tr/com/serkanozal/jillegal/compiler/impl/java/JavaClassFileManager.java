/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler.impl.java;

import java.io.IOException;
import java.security.SecureClassLoader;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaFileObject.Kind;

@SuppressWarnings("rawtypes")
class JavaClassFileManager extends ForwardingJavaFileManager {
	
    private JavaClassObject jclassObject;
	
    @SuppressWarnings("unchecked")
	JavaClassFileManager(StandardJavaFileManager standardManager) {
        super(standardManager);
    }
    
    @SuppressWarnings("unchecked")
	JavaClassFileManager(StandardJavaFileManager standardManager, String className) {
        super(standardManager);
        jclassObject = new JavaClassObject(className, Kind.CLASS);
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return new SecureClassLoader() {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                byte[] b = jclassObject.getBytes();
                String className = jclassObject.getName();
                if (className.startsWith("/") || className.startsWith("\\")) {
                	className = className.substring(1, className.length());
                }
                className = className.substring(0, className.length() - 6);
                className = className.replace("/", ".");
                if (className.equals(name)) {
                	return super.defineClass(name, jclassObject.getBytes(), 0, b.length);
                }
                else {
                	return JavaClassCompiler.class.getClassLoader().loadClass(name);
                } 
            }
        };
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, 
    		Kind kind, FileObject sibling) throws IOException {
    	jclassObject = new JavaClassObject(className, kind);
        return jclassObject;
    }
    
}
