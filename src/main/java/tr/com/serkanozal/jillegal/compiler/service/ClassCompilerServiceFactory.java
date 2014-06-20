/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler.service;

public class ClassCompilerServiceFactory {

	private static ClassCompilerService classCompilerService = new ClassCompilerServiceImpl();
	
	private ClassCompilerServiceFactory() {
		
	}
	
	public static ClassCompilerService getClassCompilerService() {
		return classCompilerService;
	}
	
	public static void setClassCompilerService(ClassCompilerService classCompilerService) {
		ClassCompilerServiceFactory.classCompilerService = classCompilerService;
	}

}
