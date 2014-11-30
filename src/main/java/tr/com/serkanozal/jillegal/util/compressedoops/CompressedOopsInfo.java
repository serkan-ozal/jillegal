/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util.compressedoops;

public class CompressedOopsInfo {

	private boolean enabled;
    private long baseAddressForObjectPointers;
    private int shiftSizeForObjectPointers;
    private long baseAddressForClassPointers;
    private int shiftSizeForClassPointers;

    public CompressedOopsInfo() {

    }

    public CompressedOopsInfo(boolean enabled) {
        this.enabled = enabled;
    }

    public CompressedOopsInfo(long baseAddressForObjectPointers,
            int shiftSizeForObjectPointers) {
        this.baseAddressForObjectPointers = baseAddressForObjectPointers;
        this.shiftSizeForObjectPointers = shiftSizeForObjectPointers;
        this.baseAddressForClassPointers = baseAddressForObjectPointers;
        this.shiftSizeForClassPointers = shiftSizeForObjectPointers;
        this.enabled = true;
    }

    public CompressedOopsInfo(long baseAddressForObjectPointers,
            int shiftSizeForObjectPointers,
            long baseAddressForClassPointers, int shiftSizeForClassPointers) {
        this.baseAddressForObjectPointers = baseAddressForObjectPointers;
        this.shiftSizeForObjectPointers = shiftSizeForObjectPointers;
        this.baseAddressForClassPointers = baseAddressForClassPointers;
        this.shiftSizeForClassPointers = shiftSizeForClassPointers;
        this.enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getBaseAddressForObjectPointers() {
        return baseAddressForObjectPointers;
    }

    public void setBaseAddressForObjectPointers(
            long baseAddressForObjectPointers) {
        this.baseAddressForObjectPointers = baseAddressForObjectPointers;
    }

    public int getShiftSizeForObjectPointers() {
        return shiftSizeForObjectPointers;
    }

    public void setShiftSizeForObjectPointers(int shiftSizeForObjectPointers) {
        this.shiftSizeForObjectPointers = shiftSizeForObjectPointers;
    }

    public long getBaseAddressForClassPointers() {
        return baseAddressForClassPointers;
    }

    public void setBaseAddressForClassPointers(
            long baseAddressForClassPointers) {
        this.baseAddressForClassPointers = baseAddressForClassPointers;
    }

    public int getShiftSizeForClassPointers() {
        return shiftSizeForClassPointers;
    }

    public void setShiftSizeForClassPointers(int shiftSizeForClassPointers) {
        this.shiftSizeForClassPointers = shiftSizeForClassPointers;
    }

    @Override
    public String toString() {
        if (enabled) {
            return "Compressed-Oops are enabled with " + "base-address: "
                    + baseAddressForObjectPointers
                    + " and with " + "shift-size: "
                    + shiftSizeForObjectPointers;
        } else {
            return "Compressed-Oops are disabled";
        }
    }
    
}
