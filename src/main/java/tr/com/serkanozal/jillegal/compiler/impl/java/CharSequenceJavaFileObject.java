/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler.impl.java;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

class CharSequenceJavaFileObject extends SimpleJavaFileObject {

	private CharSequence content;

    public CharSequenceJavaFileObject(String className,
        CharSequence content) {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.content = content;
    }

    @Override
    public CharSequence getCharContent(
        boolean ignoreEncodingErrors) {
        return content;
    }
    
}
