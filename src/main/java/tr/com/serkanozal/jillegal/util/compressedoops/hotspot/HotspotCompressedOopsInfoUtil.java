/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util.compressedoops.hotspot;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.util.compressedoops.CompressedOopsInfo;

public class HotspotCompressedOopsInfoUtil {

	private static final Logger logger = Logger.getLogger(HotspotCompressedOopsInfoUtil.class);
	
	private static final List<HotspotCompressedOopsInfoProvider> compressedOopsInfoProviderList = 
			new ArrayList<HotspotCompressedOopsInfoProvider>();
	private static CompressedOopsInfo COMPRESSED_OOPS_INFO;
	
	static {
		compressedOopsInfoProviderList.add(
				new HotspotServiceabilityAgentBasedCompressedOopsInfoProvider());
		compressedOopsInfoProviderList.add(
				new HotspotMemoryLayoutBasedCompressedOopsInfoProvider());
		compressedOopsInfoProviderList.add(
				new HotspotMxBeanBasedCompressedOopsInfoProvider());
	}
	
	private HotspotCompressedOopsInfoUtil() {
		
	}
	
	public synchronized static CompressedOopsInfo getCompressedOopsInfo(Unsafe unsafe, int oopSize, 
			int addressSize, int objectAlignment, boolean isCompressedRef) {
		if (COMPRESSED_OOPS_INFO == null) {
			COMPRESSED_OOPS_INFO = 
					findCompressedOopsInfo(unsafe, oopSize, addressSize, objectAlignment, isCompressedRef);
		}
		if (COMPRESSED_OOPS_INFO == null) {
			throw new IllegalStateException("Compressed-Oops information could not be found !");
		}
		return COMPRESSED_OOPS_INFO;
	}
	
	private static CompressedOopsInfo findCompressedOopsInfo(Unsafe unsafe, int oopSize, 
			int addressSize, int objectAlignment, boolean isCompressedRef) {
		CompressedOopsInfo foundCompressedOopsInfo = null;
		HotspotCompressedOopsInfoProvider usedCompressedOopsInfoProvider = null;
		for (HotspotCompressedOopsInfoProvider compressedOopsInfoProvider : compressedOopsInfoProviderList) {
			try {
				foundCompressedOopsInfo = 
						compressedOopsInfoProvider.
							getCompressedOopsInfo(unsafe, oopSize, addressSize, objectAlignment, isCompressedRef);
				if (foundCompressedOopsInfo != null) {
					usedCompressedOopsInfoProvider = compressedOopsInfoProvider;
					break;
				}
			} catch (Throwable t) {
				
			}
		}
		if (foundCompressedOopsInfo != null) {
			logger.info("Compressed-Oops information has been found by using " + 
					usedCompressedOopsInfoProvider.getClass().getName());
		} 
		return foundCompressedOopsInfo;
	}
	
}
