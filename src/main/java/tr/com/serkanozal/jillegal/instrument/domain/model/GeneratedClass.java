/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.domain.model;

public class GeneratedClass<T> {
	
    private Class<T> sourceClass;
    private byte[] classData;
    
    public GeneratedClass() {
        
    }
    
    public GeneratedClass(Class<T> sourceClass) {
        this.sourceClass = sourceClass;
    }
    
    public GeneratedClass(Class<T> sourceClass, byte[] classData) {
        this.sourceClass = sourceClass;
        this.classData = classData;
    }
    
    public Class<T> getSourceClass() {
        return sourceClass;
    }
    
    public void setSourceClass(Class<T> sourceClass) {
        this.sourceClass = sourceClass;
    }
    
    public byte[] getClassData() {
        return classData;
    }
    
    public void setClassData(byte[] classData) {
        this.classData = classData;
    }
    
}
