/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument;

public abstract class AbstractInstrumenter<T> implements Instrumenter<T> {
    
	protected Class<T> sourceClass;
    
    public AbstractInstrumenter(Class<T> sourceClass) {
        this.sourceClass = sourceClass;
    }
    
    public Class<T> getSourceClass() {
        return sourceClass;
    }
    
}
