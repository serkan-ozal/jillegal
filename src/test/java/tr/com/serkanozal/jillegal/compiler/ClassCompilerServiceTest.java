/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.compiler;

import org.junit.Assert;
import org.junit.Test;

import tr.com.serkanozal.jillegal.compiler.domain.model.DefaultCodeType;
import tr.com.serkanozal.jillegal.compiler.impl.groovy.GroovyClassCompiler;
import tr.com.serkanozal.jillegal.compiler.impl.java.JavaClassCompiler;
import tr.com.serkanozal.jillegal.compiler.service.ClassCompilerService;
import tr.com.serkanozal.jillegal.compiler.service.ClassCompilerServiceFactory;

public class ClassCompilerServiceTest {

	private ClassCompilerService classCompilerService = ClassCompilerServiceFactory.getClassCompilerService();
	
	@Test
	public void classCompilerRetrievedSuccessfully() {
		Assert.assertEquals(
				JavaClassCompiler.class, 
				classCompilerService.getClassCompiler(DefaultCodeType.JAVA).getClass());
		Assert.assertEquals(
				GroovyClassCompiler.class, 
				classCompilerService.getClassCompiler(DefaultCodeType.GROOVY).getClass());
	}
	
}
