/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util.compressedoops.hotspot;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import javax.management.openmbean.CompositeDataSupport;

import org.apache.log4j.Logger;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.util.compressedoops.CompressedOopsInfo;

public class HotspotMxBeanBasedCompressedOopsInfoProvider implements HotspotCompressedOopsInfoProvider {

	private static final Logger logger = 
			Logger.getLogger(HotspotMxBeanBasedCompressedOopsInfoProvider.class);
	
	@Override
	public CompressedOopsInfo getCompressedOopsInfo(Unsafe unsafe, int oopSize, 
			int addressSize, int objectAlignment, boolean isCompressedRef) {
		if (oopSize == unsafe.addressSize()) {
            return new CompressedOopsInfo(false);
        }
		if (isHotspotJvm()) {
			return findHotspotCompressedOopsInfo(unsafe, oopSize, addressSize);
		} 
		return null;
	}
	
	private boolean isHotspotJvm() {
		String name = System.getProperty("java.vm.name").toLowerCase();
		return name.contains("hotspot") || name.contains("openjdk");
	}
	
	private int log2p(int x) {
        int r = 0;
        while ((x >>= 1) != 0) {
            r++;
        }    
        return r;
    }
	
	private CompressedOopsInfo findHotspotCompressedOopsInfo(Unsafe unsafe, int oopSize, int addressSize) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();

            try {
                ObjectName mbean = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
                CompositeDataSupport compressedOopsValue = 
                		(CompositeDataSupport) server.invoke(
                				mbean, 
                				"getVMOption", 
                				new Object[] { "UseCompressedOops" }, 
                				new String[] { "java.lang.String" });
                boolean compressedOops = Boolean.valueOf(compressedOopsValue.get("value").toString());
                if (compressedOops) {
                    // If compressed oops are enabled, then this option is also accessible
                    CompositeDataSupport alignmentValue = 
                    		(CompositeDataSupport) server.invoke(
                    				mbean, 
                    				"getVMOption", 
                    				new Object[] { "ObjectAlignmentInBytes" }, 
                    				new String[] { "java.lang.String" });
                    int align = Integer.valueOf(alignmentValue.get("value").toString());
                    return new CompressedOopsInfo(0, log2p(align));
                } 
                else {
                    return new CompressedOopsInfo(false);
                }
            } 
            catch (RuntimeMBeanException iae) {
                return new CompressedOopsInfo(false);
            }
        } 
        catch (Exception e) {
        	logger.error("Failed to read HotSpot-specific configuration properly", e);
            return null;
        } 
    }
	
}
