/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.filter;

import tr.com.serkanozal.jillegal.instrument.domain.model.ClassInfo;

public interface ClassTransformerFilter {

	byte[] doFilter(ClassInfo ci);

}
