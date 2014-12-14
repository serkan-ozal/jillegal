/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util.compressedoops.hotspot;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.util.compressedoops.CompressedOopsInfo;

@SuppressWarnings( { "restriction" } )
public interface HotspotCompressedOopsInfoProvider {

	CompressedOopsInfo getCompressedOopsInfo(Unsafe unsafe, int oopSize, 
			int addressSize, int objectAlignment, boolean isCompressedRef);
	
}
