/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler.domain.model;

public enum DefaultCodeType implements CodeType {

	JAVA(0, "JAVA"),
	GROOVY(1, "GROOVY");
	
	int code;
	String typeName;
	
	private DefaultCodeType(int code, String typeName) {
		this.code = code;
		this.typeName = typeName;
	}
	
	public int getCode() {
		return code;
	}
	
	@Override
	public String getTypeName() {
		return typeName;
	}
	
	public static DefaultCodeType getClassType(int code) {
		for (DefaultCodeType ct : DefaultCodeType.values()) {
			if (ct.code == code) {
				return ct;
			}
		}
		return null;
	}

}
