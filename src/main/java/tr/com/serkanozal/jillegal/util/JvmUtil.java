/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import javax.management.openmbean.CompositeDataSupport;

import org.apache.log4j.Logger;

import sun.misc.Unsafe;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * @link http://hg.openjdk.java.net/jdk7/hotspot/hotspot/file/9b0ca45cd756/src/share/vm/oops/oop.hpp
 * @link http://hg.openjdk.java.net/jdk7/hotspot/hotspot/file/9b0ca45cd756/src/share/vm/oops/klass.hpp
 */
@SuppressWarnings("restriction")
public class JvmUtil {

	public static final byte SIZE_32_BIT = 4;
    public static final byte SIZE_64_BIT = 8;
    public static final byte INVALID_ADDRESS = -1;

    public static final int NR_BITS = Integer.valueOf(System.getProperty("sun.arch.data.model"));
    public static final int BYTE = 8;
    public static final int WORD = NR_BITS / BYTE;
    public static final int MIN_SIZE = 16; 
    
    public static final int ADDRESS_SHIFT_SIZE_32_BIT = 0; 
    public static final int ADDRESS_SHIFT_SIZE_64_BIT = 3; 
    
    public static final int OBJECT_HEADER_SIZE_32_BIT = 8; 
    public static final int OBJECT_HEADER_SIZE_64_BIT = 12; 
    
    public static final int CLASS_DEF_POINTER_OFFSET_IN_OBJECT_FOR_32_BIT = 4;
    public static final int CLASS_DEF_POINTER_OFFSET_IN_OBJECT_FOR_64_BIT = 8;
    
    public static final int CLASS_DEF_POINTER_OFFSET_IN_CLASS_32_BIT = 8; 
    public static final int CLASS_DEF_POINTER_OFFSET_IN_CLASS_64_BIT = 12;
    
    public static final int SIZE_FIELD_OFFSET_IN_CLASS_32_BIT = 12;
    public static final int SIZE_FIELD_OFFSET_IN_CLASS_64_BIT = 24;
    
    public static final int BOOLEAN_SIZE = 1;
    public static final int BYTE_SIZE = Byte.SIZE / NR_BITS;
    public static final int CHAR_SIZE = Character.SIZE / NR_BITS;
    public static final int SHORT_SIZE = Short.SIZE / NR_BITS;
    public static final int INT_SIZE = Integer.SIZE / NR_BITS;
    public static final int FLOAT_SIZE = Float.SIZE / NR_BITS;
    public static final int LONG_SIZE = Long.SIZE / NR_BITS;
    public static final int DOUBLE_SIZE = Double.SIZE / NR_BITS;
    
    private static final Logger logger = Logger.getLogger(JvmUtil.class);
    
    private static VMOptions options;
    private static Unsafe unsafe;
    private static int addressSize;
    private static int headerSize;
    private static int arrayHeaderSize;
    private static long baseOffset;
    private static int indexScale;
	private static int classDefPointerOffsetInObject;
    private static int classDefPointerOffsetInClass;
    private static int sizeFieldOffsetOffsetInClass;
    
    static {
    	init();
    }
	
	private static void init() {
        // steal Unsafe
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
        } 
        catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        } 
        catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

        int headerSize;
        try {
            long off1 = unsafe.objectFieldOffset(HeaderClass.class.getField("b1"));
            headerSize = (int) off1;
        } 
        catch (NoSuchFieldException e) {
            headerSize = -1;
        }

        JvmUtil.addressSize = unsafe.addressSize();
        JvmUtil.headerSize = headerSize;
        JvmUtil.arrayHeaderSize = headerSize + (Integer.SIZE / NR_BITS);
        JvmUtil.options = findOptions();
        
        JvmUtil.baseOffset = unsafe.arrayBaseOffset(Object[].class);
        JvmUtil.indexScale = unsafe.arrayIndexScale(Object[].class);
        
        switch (addressSize) {
            case SIZE_32_BIT:
            	JvmUtil.classDefPointerOffsetInObject = CLASS_DEF_POINTER_OFFSET_IN_OBJECT_FOR_32_BIT;
            	JvmUtil.classDefPointerOffsetInClass = CLASS_DEF_POINTER_OFFSET_IN_CLASS_32_BIT;
            	JvmUtil.sizeFieldOffsetOffsetInClass = SIZE_FIELD_OFFSET_IN_CLASS_32_BIT;
                break;
                
            case SIZE_64_BIT:
            	JvmUtil.classDefPointerOffsetInObject = CLASS_DEF_POINTER_OFFSET_IN_OBJECT_FOR_64_BIT;
            	JvmUtil.classDefPointerOffsetInClass = CLASS_DEF_POINTER_OFFSET_IN_CLASS_64_BIT;
            	JvmUtil.sizeFieldOffsetOffsetInClass = SIZE_FIELD_OFFSET_IN_CLASS_64_BIT;
                break;    
        }        
    }
	
	public static Unsafe getUnsafe() {
		return unsafe;
	}
	
	public static VMOptions getOptions() {
		return options;
	}
	
	public static int getAddressSize() {
		return addressSize;
	}
	
	public static int getHeaderSize() {
		return headerSize;
	}
	
	public static int getArrayHeaderSize() {
		return arrayHeaderSize;
	}
	
	public static long getBaseOffset() {
		return baseOffset;
	}
	
	public static int getIndexScale() {
		return indexScale;
	}
	
	public static int getClassDefPointerOffsetInClass() {
		return classDefPointerOffsetInClass;
	}
	
	public static int getClassDefPointerOffsetInObject() {
		return classDefPointerOffsetInObject;
	}
	
	public static int getSizeFieldOffsetOffsetInClass() {
		return sizeFieldOffsetOffsetInClass;
	}
	
    private static long normalize(int value) {
        if (value >= 0) {
            return value;
        }    
        else {
            return (~0L >>> 32) & value;
        }    
    }
    
    public static long internalAddressOf(Object obj) {
        return normalize(System.identityHashCode(obj));
    }
    
    public static int sizeOfType(Class<?> type) {
    	if (type == boolean.class) { 
        	return BOOLEAN_SIZE; 
        }
    	else if (type == byte.class) { 
        	return BYTE_SIZE; 
        }
    	else if (type == char.class) { 
        	return CHAR_SIZE;
        }
        else if (type == short.class) { 
        	return SHORT_SIZE;
        }
        else if (type == int.class) { 
        	return INT_SIZE;
        }
        else if (type == float.class) { 
        	return FLOAT_SIZE;
        }
        else if (type == long.class) { 
        	return LONG_SIZE;
        }
        else if (type == double.class) { 
        	return DOUBLE_SIZE;
        }
        else {
        	return options.referenceSize;
        }	
    }
    
	public static int sizeOfArray(Object o) {
        int base = unsafe.arrayBaseOffset(o.getClass());
        int scale = unsafe.arrayIndexScale(o.getClass());
        Class<?> type = o.getClass().getComponentType();
        if (type == boolean.class) {
        	return base + ((boolean[]) o).length * scale;
        }
        if (type == byte.class) {
        	return base + ((byte[]) o).length * scale;
        }
        if (type == short.class) {
        	return base + ((short[]) o).length * scale;
        }
        if (type == char.class) {
        	return base + ((char[]) o).length * scale;
        }
        if (type == int.class) {
        	return base + ((int[]) o).length * scale;
        }
        if (type == float.class) {
        	return base + ((float[]) o).length * scale;
        }
        if (type == long.class) {
        	return base + ((long[]) o).length * scale;
        }
        if (type == double.class) {
        	return base + ((double[]) o).length * scale;
        }
        return base + ((Object[]) o).length * scale;
    }
	
	public static long sizeOfArray(Class<?> elementClass, long elementCount) {
		Object array = Array.newInstance(elementClass, 0);
		int base = unsafe.arrayBaseOffset(array.getClass());
        int scale = unsafe.arrayIndexScale(array.getClass());
    	return base + elementCount * scale;
    }
	
	public static int arrayBaseOffset(Class<?> elementClass) {
		Object array = Array.newInstance(elementClass, 0);
		return unsafe.arrayBaseOffset(array.getClass());
    }
	
	public static int arrayIndexScale(Class<?> elementClass) {
		Object array = Array.newInstance(elementClass, 0);
		return unsafe.arrayIndexScale(array.getClass());
    }
	
	public static int arrayLengthSize() {
		return INT_SIZE;
	}
    
    public static long toNativeAddress(long address) {
    	return options.toNativeAddress(address);
    }
    
    public static long toJvmAddress(long address) {
    	return options.toJvmAddress(address);
    }
    
    public static void dumpFromObject(Object obj, long size) {
    	for (int i = 0; i < size; i++) {
	    	System.out.print(String.format("%02x ", unsafe.getByte(obj, (long)i)));
			if ((i + 1) % 16 == 0) {
				System.out.println();
			}
    	}	
    	System.out.println();
    }
    
    public static void info() {
        System.out.println("Running " + (addressSize * NR_BITS) + "-bit " + options.name + " VM.");
        if (options.compressedRef) {
        	System.out.println("Using compressed references with " + options.compressRefShift + "-bit shift.");
        }
        System.out.println("Objects are " + options.objectAlignment + " bytes aligned.");
        System.out.println();
    }

    private static VMOptions findOptions() {
        // try Hotspot
        VMOptions hsOpts = getHotspotSpecifics();
        if (hsOpts != null) {
        	return hsOpts;
        }

        // try JRockit
        VMOptions jrOpts = getJRockitSpecifics();
        if (jrOpts != null) {
        	return jrOpts;
        }

        // When running with CompressedOops on 64-bit platform, the address size
        // reported by Unsafe is still 8, while the real reference fields are 4 bytes long.
        // Try to guess the reference field size with this naive trick.
        int oopSize;
        try {
            long off1 = unsafe.objectFieldOffset(CompressedOopsClass.class.getField("obj1"));
            long off2 = unsafe.objectFieldOffset(CompressedOopsClass.class.getField("obj2"));
            oopSize = (int) Math.abs(off2 - off1);
        } 
        catch (NoSuchFieldException e) {
            oopSize = -1;
        }

        if (oopSize != unsafe.addressSize()) {
            return new VMOptions("Auto-detected", ADDRESS_SHIFT_SIZE_64_BIT); // assume compressed references have << 3 shift
        } 
        else {
            return new VMOptions("Auto-detected", ADDRESS_SHIFT_SIZE_32_BIT);
        }
    }
    
    private static VMOptions getHotspotSpecifics() {
        String name = System.getProperty("java.vm.name");
        if (!name.contains("HotSpot") && !name.contains("OpenJDK")) {
            return null;
        }

        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();

            try {
                ObjectName mbean = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
                CompositeDataSupport compressedOopsValue = 
                		(CompositeDataSupport) server.invoke(mbean, "getVMOption", 
                				new Object[]{"UseCompressedOops"}, new String[]{"java.lang.String"});
                boolean compressedOops = Boolean.valueOf(compressedOopsValue.get("value").toString());
                if (compressedOops) {
                    // if compressed oops are enabled, then this option is also accessible
                    CompositeDataSupport alignmentValue = 
                    		(CompositeDataSupport) server.invoke(mbean, "getVMOption", 
                    				new Object[]{"ObjectAlignmentInBytes"}, new String[]{"java.lang.String"});
                    int align = Integer.valueOf(alignmentValue.get("value").toString());
                    return new VMOptions("HotSpot", log2p(align));
                } 
                else {
                    return new VMOptions("HotSpot");
                }

            } 
            catch (RuntimeMBeanException iae) {
                return new VMOptions("HotSpot");
            }
        } 
        catch (RuntimeException e) {
        	logger.error("Failed to read HotSpot-specific configuration properly, please report this as the bug", e);
            return null;
        } 
        catch (Exception e) {
        	logger.error("Failed to read HotSpot-specific configuration properly, please report this as the bug", e);
            return null;
        }
    }

    private static VMOptions getJRockitSpecifics() {
        String name = System.getProperty("java.vm.name");
        if (!name.contains("JRockit")) {
            return null;
        }

        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            String str = (String) server.invoke(new ObjectName("oracle.jrockit.management:type=DiagnosticCommand"), "execute", new Object[]{"print_vm_state"}, new String[]{"java.lang.String"});
            String[] split = str.split("\n");
            for (String s : split) {
                if (s.contains("CompRefs")) {
                    Pattern pattern = Pattern.compile("(.*?)References are compressed, with heap base (.*?) and shift (.*?)\\.");
                    Matcher matcher = pattern.matcher(s);
                    if (matcher.matches()) {
                        return new VMOptions("JRockit", Integer.valueOf(matcher.group(3)));
                    } 
                    else {
                        return new VMOptions("JRockit");
                    }
                }
            }
            return null;
        } 
        catch (RuntimeException e) {
        	logger.error("Failed to read JRockit-specific configuration properly, please report this as the bug", e);
            return null;
        } 
        catch (Exception e) {
        	logger.error("Failed to read JRockit-specific configuration properly, please report this as the bug", e);
            return null;
        }
    }
    
    @SuppressWarnings("unused")
	private static int align(int addr) {
        int align = options.objectAlignment;
        if ((addr % align) == 0) {
            return addr;
        } 
        else {
            return ((addr / align) + 1) * align;
        }
    }

    private static int log2p(int x) {
        int r = 0;
        while ((x >>= 1) != 0) {
            r++;
        }    
        return r;
    }
    
    private static int guessAlignment(int oopSize) {
        final int COUNT = 1000 * 1000;
        Object[] array = new Object[COUNT];
        long[] offsets = new long[COUNT];

        for (int c = 0; c < COUNT - 3; c += 3) {
            array[c + 0] = new MyObject1();
            array[c + 1] = new MyObject2();
            array[c + 1] = new MyObject3();
        }

        for (int c = 0; c < COUNT; c++) {
            offsets[c] = addressOf(array[c], oopSize);
        }

        Arrays.sort(offsets);

        Multiset<Integer> sizes = HashMultiset.create();
        for (int c = 1; c < COUNT; c++) {
            sizes.add((int) (offsets[c] - offsets[c - 1]));
        }

        int min = -1;
        for (int s : sizes.elementSet()) {
            if (s <= 0) {
            	continue;
            }
            if (min == -1) {
                min = s;
            } 
            else {
                min = gcd(min, s);
            }
        }

        return min;
    }
    
    private static long addressOf(Object o, int oopSize) {
        Object[] array = new Object[]{o};

        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
        long objectAddress;
        switch (oopSize) {
            case SIZE_32_BIT:
                objectAddress = unsafe.getInt(array, baseOffset);
                break;
            case SIZE_64_BIT:
                objectAddress = unsafe.getLong(array, baseOffset);
                break;
            default:
                throw new Error("Unsupported address size: " + oopSize);
        }

        return objectAddress;
    }

    private static int gcd(int a, int b) {
        while (b > 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    
    public static class VMOptions {
    	
        private final String name;
        private final boolean compressedRef;
        private final int compressRefShift;
        private final int objectAlignment;
        private final int referenceSize;

        public VMOptions(String name) {
            this.name = name;
            this.referenceSize = unsafe.addressSize();
            this.objectAlignment = guessAlignment(this.referenceSize);
            this.compressedRef = false;
            this.compressRefShift = ADDRESS_SHIFT_SIZE_32_BIT;
        }

        public VMOptions(String name, int shift) {
            this.name = name;
            this.referenceSize = SIZE_32_BIT;
            this.objectAlignment = guessAlignment(this.referenceSize) << shift;
            this.compressedRef = true;
            this.compressRefShift = shift;
        }

        public long toNativeAddress(long address) {
            if (compressedRef) {
                return address << compressRefShift;
            } 
            else {
                return address;
            }
        }
        
        public long toJvmAddress(long address) {
            if (compressedRef) {
                return address >> compressRefShift;
            } 
            else {
                return address;
            }
        }

		public String getName() {
			return name;
		}

		public boolean isCompressedRef() {
			return compressedRef;
		}

		public int getCompressRefShift() {
			return compressRefShift;
		}

		public int getObjectAlignment() {
			return objectAlignment;
		}

		public int getReferenceSize() {
			return referenceSize;
		}

    }
    
    @SuppressWarnings("unused")
    private static class CompressedOopsClass {
        
		public Object obj1;
        public Object obj2;
        
    }

    @SuppressWarnings("unused")
    private static class HeaderClass {
    	
        public boolean b1;
        
    }
    
    private static class MyObject1 {

    }

    @SuppressWarnings("unused")
    private static class MyObject2 {
    	
        private boolean b1;
        
    }

    @SuppressWarnings("unused")
    private static class MyObject3 {
    	
        private int i1;
        
    }
    
}
