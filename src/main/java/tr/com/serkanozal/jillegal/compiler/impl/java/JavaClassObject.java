/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler.impl.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

class JavaClassObject extends SimpleJavaFileObject {
	
	private String code;
	private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
	
	JavaClassObject(String name, String code) {
		super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
	    this.code = code;
	}
	
	JavaClassObject(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }

	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return code;
	}
	
	public byte[] getBytes() {
        return bos.toByteArray();
    }
	
	@Override
    public OutputStream openOutputStream() throws IOException {
        return bos;
    }
	
}
