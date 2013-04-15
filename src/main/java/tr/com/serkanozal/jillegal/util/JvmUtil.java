/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util;

import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.TreeMap;
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
 * 
 * @link https://blogs.oracle.com/jrockit/entry/understanding_compressed_refer
 * @link https://wikis.oracle.com/display/HotSpotInternals/CompressedOops
 * 
 * Note: Use "-XX:-UseCompressedOops" for 64 bit JVM to disable CompressedOops
 */
@SuppressWarnings("restriction")
public class JvmUtil {

	public static final String JAVA_VERSION = System.getProperty("java.version");
	public static final String JAVA_VENDOR = System.getProperty("java.vendor");
	public static final String JVM_VENDOR = System.getProperty("java.vm.vendor");
	public static final String JVM_VERSION = System.getProperty("java.vm.version");
	public static final String JVM_NAME = System.getProperty("java.vm.name");
	public static final String OS_ARCH = System.getProperty("os.arch");
	public static final String OS_VERSION = System.getProperty("os.version");
	  
	public static final byte SIZE_32_BIT = 4;
    public static final byte SIZE_64_BIT = 8;
    public static final byte INVALID_ADDRESS = -1;
    
    public static final byte ADDRESSING_4_BYTE = 4;
    public static final byte ADDRESSING_8_BYTE = 8;
    public static final byte ADDRESSING_16_BYTE = 16;

    public static final int NR_BITS = Integer.valueOf(System.getProperty("sun.arch.data.model"));
    public static final int BYTE = 8;
    public static final int WORD = NR_BITS / BYTE;
    public static final int MIN_SIZE = 16; 
    
    public static final int ADDRESS_SHIFT_SIZE_FOR_BETWEEN_32GB_AND_64_GB = 3; 
    public static final int ADDRESS_SHIFT_SIZE_FOR_BIGGER_THAN_64_GB = 4; 
    
    public static final int OBJECT_HEADER_SIZE_32_BIT = 8; 
    public static final int OBJECT_HEADER_SIZE_64_BIT = 12; 
    
    public static final int CLASS_DEF_POINTER_OFFSET_IN_OBJECT_FOR_32_BIT = 4;
    public static final int CLASS_DEF_POINTER_OFFSET_IN_OBJECT_FOR_64_BIT = 8;
    
    public static final int CLASS_DEF_POINTER_OFFSET_IN_CLASS_32_BIT = 8; 
    public static final int CLASS_DEF_POINTER_OFFSET_IN_CLASS_64_BIT = 12;
    
    public static final int SIZE_FIELD_OFFSET_IN_CLASS_32_BIT = 12;
    public static final int SIZE_FIELD_OFFSET_IN_CLASS_64_BIT = 24;
    
    public static final int BOOLEAN_SIZE = 1;
    public static final int BYTE_SIZE = Byte.SIZE / BYTE;
    public static final int CHAR_SIZE = Character.SIZE / BYTE;
    public static final int SHORT_SIZE = Short.SIZE / BYTE;
    public static final int INT_SIZE = Integer.SIZE / BYTE;
    public static final int FLOAT_SIZE = Float.SIZE / BYTE;
    public static final int LONG_SIZE = Long.SIZE / BYTE;
    public static final int DOUBLE_SIZE = Double.SIZE / BYTE;
    
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
    
    private static final Map<Class<?>, ClassCache> classCache = new HashMap<Class<?>, ClassCache>();
    
    static {
    	init();
    }
	
	private static void init() {
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
        JvmUtil.baseOffset = unsafe.arrayBaseOffset(Object[].class);
        JvmUtil.indexScale = unsafe.arrayIndexScale(Object[].class);
        JvmUtil.headerSize = headerSize;
        JvmUtil.arrayHeaderSize = headerSize + indexScale;
        JvmUtil.options = findOptions();

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
                
            default:
            	throw new AssertionError("Unsupported address size: " + addressSize); 
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
	
	public static boolean isCompressedRef() {
		return options.compressedRef;
	}
	
	public static int getReferenceSize() {
		return options.referenceSize;
	}
	
	public static int getObjectAlignment() {
		return options.objectAlignment;
	}
	
	public static int getCompressedReferenceShift() {
		return options.compressRefShift;
	}
	
	public static String getVmName() {
		return options.name;
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
    
    public static boolean isPrimitiveType(Class<?> type) {
    	if (type == boolean.class) { 
        	return true; 
        }
    	else if (type == byte.class) { 
        	return true; 
        }
    	else if (type == char.class) { 
        	return true;
        }
        else if (type == short.class) { 
        	return true;
        }
        else if (type == int.class) { 
        	return true;
        }
        else if (type == float.class) { 
        	return true;
        }
        else if (type == long.class) { 
        	return true;
        }
        else if (type == double.class) { 
        	return true;
        }
        else {
        	return false;
        }	
    }
    
    public static boolean isComplexType(Class<?> type) {
    	return !isPrimitiveType(type);
    }
    
    public static Class<?> primitiveTypeOf(Class<?> type) {
    	if (isPrimitiveType(type)) {
    		return type;
    	}
    	
    	if (type == Boolean.class) { 
        	return boolean.class; 
        }
    	else if (type == Byte.class) { 
        	return byte.class; 
        }
    	else if (type == Character.class) { 
        	return char.class;
        }
        else if (type == Short.class) { 
        	return short.class;
        }
        else if (type == Integer.class) { 
        	return int.class;
        }
        else if (type == Float.class) { 
        	return float.class;
        }
        else if (type == Long.class) { 
        	return long.class;
        }
        else if (type == Double.class) { 
        	return double.class;
        }
        else {
        	return null;
        }	
    }
    
    public static Class<?> complexTypeOf(Class<?> type) {
    	if (type == boolean.class) { 
        	return Boolean.class; 
        }
    	else if (type == byte.class) { 
        	return Byte.class; 
        }
    	else if (type == char.class) { 
        	return Character.class;
        }
        else if (type == short.class) { 
        	return Short.class;
        }
        else if (type == int.class) { 
        	return Integer.class;
        }
        else if (type == float.class) { 
        	return Float.class;
        }
        else if (type == long.class) { 
        	return Long.class;
        }
        else if (type == double.class) { 
        	return Double.class;
        }
        else {
        	return type;
        }	
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
        else if (type == byte.class) {
        	return base + ((byte[]) o).length * scale;
        }
        else if (type == short.class) {
        	return base + ((short[]) o).length * scale;
        }
        else if (type == char.class) {
        	return base + ((char[]) o).length * scale;
        }
        else if (type == int.class) {
        	return base + ((int[]) o).length * scale;
        }
        else if (type == float.class) {
        	return base + ((float[]) o).length * scale;
        }
        else if (type == long.class) {
        	return base + ((long[]) o).length * scale;
        }
        else if (type == double.class) {
        	return base + ((double[]) o).length * scale;
        }
        else {
        	return base + ((Object[]) o).length * scale;
        }	
    }
	
	public static long sizeOfArray(Class<?> elementClass, long elementCount) {
    	return arrayBaseOffset(elementClass) + (elementCount * arrayIndexScale(elementClass));
    }
	
	public static int arrayBaseOffset(Class<?> elementClass) {
		return getCacheEntry(elementClass).arrayBaseOffset;
    }
	
	public static int arrayIndexScale(Class<?> elementClass) {
		return getCacheEntry(elementClass).arrayIndexScale;
    }
	
	public static int arrayLengthSize() {
		return options.referenceSize;
	}
	
	public static int getArrayLength(long arrayStartAddress, Class<?> elementType) {
		long arrayIndexStartAddress = arrayStartAddress + JvmUtil.arrayBaseOffset(elementType);
		int referenceSize = JvmUtil.getReferenceSize();
		switch (referenceSize) {
			case JvmUtil.ADDRESSING_4_BYTE:
				return unsafe.getInt(arrayIndexStartAddress - JvmUtil.arrayLengthSize());
			case JvmUtil.ADDRESSING_8_BYTE:
				return (int)unsafe.getLong(arrayIndexStartAddress - JvmUtil.arrayLengthSize());
			case JvmUtil.ADDRESSING_16_BYTE:
				return (int)unsafe.getLong(arrayIndexStartAddress - JvmUtil.arrayLengthSize() + JvmUtil.LONG_SIZE);
			default:
				throw new AssertionError("Unsupported reference size: " + referenceSize); 
		}
	}
	
	public static void setArrayLength(long arrayStartAddress, Class<?> elementType, int length) {
		long arrayIndexStartAddress = arrayStartAddress + JvmUtil.arrayBaseOffset(elementType);
		int referenceSize = JvmUtil.getReferenceSize();
		switch (referenceSize) {
			case JvmUtil.ADDRESSING_4_BYTE:
				unsafe.putInt(arrayIndexStartAddress - JvmUtil.arrayLengthSize(), length);
				break;
			case JvmUtil.ADDRESSING_8_BYTE:
				unsafe.putLong(arrayIndexStartAddress - JvmUtil.arrayLengthSize(), length);
				break;
			case JvmUtil.ADDRESSING_16_BYTE:
				unsafe.putLong(arrayIndexStartAddress - JvmUtil.arrayLengthSize(), 0L);
				unsafe.putLong(arrayIndexStartAddress - JvmUtil.arrayLengthSize() + JvmUtil.LONG_SIZE, length);
				break;	
			default:
				throw new AssertionError("Unsupported reference size: " + referenceSize); 
		}
	}
    
    public static long toNativeAddress(long address) {
    	return options.toNativeAddress(address);
    }
    
    public static long toJvmAddress(long address) {
    	return options.toJvmAddress(address);
    }
    
    public static void dump(long address, long size) {
    	for (int i = 0; i < size; i++) {
	    	System.out.print(String.format("%02x ", unsafe.getByte(address + i)));
			if ((i + 1) % 16 == 0) {
				System.out.println();
			}
    	}	
    	System.out.println();
    }
    
    public static void dump(Object obj, long size) {
    	for (int i = 0; i < size; i++) {
	    	System.out.print(String.format("%02x ", unsafe.getByte(obj, (long)i)));
			if ((i + 1) % 16 == 0) {
				System.out.println();
			}
    	}	
    	System.out.println();
    }
    
    public static void dump(PrintWriter pw, Object root) {
		ObjectTree.dump(pw, root);
	}

	public static String dump(Object root) {
		return ObjectTree.dump(root);
	}
    
    public static String objectMemoryAsString(Object o) {
        final ByteOrder byteOrder = ByteOrder.nativeOrder();
        
        StringBuilder b = new StringBuilder();
        final int obSize = (int) shallowSizeOf(o); 
        
        for (int i = 0; i < obSize; i += 2) {
        	if ((i & 0xf) == 0) {
        		if (i > 0) {
        			b.append("\n");
        		}
        		b.append(String.format("%#06x", i));
        	}
      
        	// We go short by short because J9 fails on odd addresses (everything is aligned, including byte fields.
        	int shortValue = unsafe.getShort(o, (long) i);
      
        	if (byteOrder == ByteOrder.BIG_ENDIAN) {
        		b.append(String.format(" %02x", (shortValue >>> 8) & 0xff));
        		b.append(String.format(" %02x", (shortValue & 0xff)));
        	} 
        	else {
        		b.append(String.format(" %02x", (shortValue & 0xff)));
        		b.append(String.format(" %02x", (shortValue >>> 8) & 0xff));
        	}
        }
        return b.toString();
	}
	
	@SuppressWarnings({"unchecked"})
	public static String fieldsLayoutAsString(Class<?> clazz) {
        TreeMap<Long, String> fields = new TreeMap<Long, String>(); 
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
        	for (Field f : c.getDeclaredFields()) {
	            fields.put(
	                unsafe.objectFieldOffset(f),
	                f.getDeclaringClass().getSimpleName() + "." + f.getName());
        	}
        }
        fields.put(shallowSizeOfInstance(clazz), "#shallowSizeOfInstance(" + clazz.getName() + ")");

        StringBuilder b = new StringBuilder();
        Object [] entries = fields.entrySet().toArray();
        for (int i = 0; i < entries.length; i++) {
        	Map.Entry<Long, String> e    = (Map.Entry<Long, String>) entries[i];
        	Map.Entry<Long, String> next = (i + 1 < entries.length ? (Map.Entry<Long, String>) entries[i + 1] : null);
      
          b.append(String.format(
              "@%02d %2s %s\n", 
              e.getKey(),
              next == null ? "" : next.getKey() - e.getKey(),
              e.getValue()));
        }
        return b.toString();
	}
	
	public static long alignObjectSize(long size) {
		size += (long) options.getObjectAlignment() - 1L;
	    return size - (size % options.getObjectAlignment());
	}

	public static long sizeOf(Object obj) {
	    ArrayList<Object> stack = new ArrayList<Object>();
	    stack.add(obj);
	    return measureSizeOf(stack);
	}

	public static long sizeOfAll(Object... objects) {
	    return sizeOfAll(Arrays.asList(objects));
	}

	public static long sizeOfAll(Iterable<Object> objects) {
	    final ArrayList<Object> stack;
	    if (objects instanceof Collection<?>) {
	    	stack = new ArrayList<Object>(((Collection<?>) objects).size());
	    } 
	    else {
	    	stack = new ArrayList<Object>();
	    }

	    for (Object o : objects) {
	    	stack.add(o);
	    }

	    return measureSizeOf(stack);
	}

	public static long shallowSizeOf(Object obj) {
		if (obj == null) {
			return 0;
		}
	    final Class<?> clz = obj.getClass();
	    if (clz.isArray()) {
	    	return shallowSizeOfArray(obj);
	    } 
	    else {
	    	return sizeOf(clz);
	    }
	}
	
	public static long sizeOf(Class<?> clazz) {
		return getCacheEntry(clazz).size;
	}

	public static long shallowSizeOfAll(Object... objects) {
		return shallowSizeOfAll(Arrays.asList(objects));
	}

	public static long shallowSizeOfAll(Iterable<Object> objects) {
	    long sum = 0;
	    for (Object o : objects) {
	    	sum += shallowSizeOf(o);
	    }
	    return sum;
	}

	private static long shallowSizeOfInstance(Class<?> clazz) {
	    if (clazz.isArray()) {
	    	throw new IllegalArgumentException("This method does not work with array classes.");
	    }  
	    if (clazz.isPrimitive()) {
	    	return sizeOfType(clazz);
	    }
	    long size = headerSize;
	    
	    for (;clazz != null; clazz = clazz.getSuperclass()) {
	    	final Field[] fields = clazz.getDeclaredFields();
	    	for (Field f : fields) {
	    		if (!Modifier.isStatic(f.getModifiers())) {
	    			size = adjustForField(size, f);
	    		}
	    	}
	    }
	    return alignObjectSize(size);    
	}
	
	private static long shallowSizeOfArray(Object array) {
		long size = arrayHeaderSize;
	    final int len = Array.getLength(array);
	    if (len > 0) {
	    	Class<?> arrayElementClazz = array.getClass().getComponentType();
	    	if (arrayElementClazz.isPrimitive()) {
	    		size += (long) len * sizeOfType(arrayElementClazz);
	    	} 
	    	else {
	    		size += (long) options.referenceSize * len;
	    	}
	    }
	    return alignObjectSize(size);
	}

	private static long measureSizeOf(ArrayList<Object> stack) {
	    final IdentityHashSet<Object> seen = new IdentityHashSet<Object>();
	    final IdentityHashMap<Class<?>, ClassCache> classCache = new IdentityHashMap<Class<?>, ClassCache>();

	    long totalSize = 0;
	    while (!stack.isEmpty()) {
	    	final Object ob = stack.remove(stack.size() - 1);

	    	if (ob == null || seen.contains(ob)) {
	    		continue;
	    	}
	    	seen.add(ob);

	    	final Class<?> obClazz = ob.getClass();
	    	if (obClazz.isArray()) {
	    		/*
	    		 * Consider an array, possibly of primitive types. Push any of its references to
	    		 * the processing stack and accumulate this array's shallow size. 
	    		 */
	    		long size = arrayHeaderSize;
	    		final int len = Array.getLength(ob);
	    		if (len > 0) {
	    			Class<?> componentClazz = obClazz.getComponentType();
	    			if (componentClazz.isPrimitive()) {
	    				size += (long) len * sizeOfType(componentClazz);
	    			} 
	    			else {
	    				size += (long) options.referenceSize * len;

	    				for (int i = len; --i >= 0 ;) {
	    					final Object o = Array.get(ob, i);
	    					if (o != null && !seen.contains(o)) {
	    						stack.add(o);
	    					}
	    				}            
	    			}
	    		}
	    		totalSize += alignObjectSize(size);
	    	} 
	    	else {
	    		/*
	    		 * Consider an object. Push any references it has to the processing stack
	    		 * and accumulate this object's shallow size. 
	    		 */
	    		try {
	    			ClassCache cachedInfo = classCache.get(obClazz);
	    			if (cachedInfo == null) {
	    				classCache.put(obClazz, cachedInfo = createCacheEntry(obClazz));
	    			}

	    			for (Field f : cachedInfo.referenceFields) {
	    				// Fast path to eliminate redundancies.
	    				final Object o = f.get(ob);
	    				if (o != null && !seen.contains(o)) {
	    					stack.add(o);
	    				}
	    			}

	    			totalSize += cachedInfo.alignedShallowInstanceSize;
	    		} 
	    		catch (IllegalAccessException e) {
	    			// This should never happen as we enabled setAccessible().
	    			throw new RuntimeException("Reflective field access failed?", e);
	    		}
	    	}
	    }

	    // Help the GC.
	    seen.clear();
	    stack.clear();
	    classCache.clear();

	    return totalSize;
	}
	
	private static ClassCache createCacheEntry(final Class<?> clazz) {
	    ClassCache cachedInfo;
	    long shallowInstanceSize = headerSize;
	    final ArrayList<Field> referenceFields = new ArrayList<Field>(32);
	    for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
	    	final Field[] fields = c.getDeclaredFields();
	    	for (final Field f : fields) {
	    		if (!Modifier.isStatic(f.getModifiers())) {
	    			shallowInstanceSize = adjustForField(shallowInstanceSize, f);

	    			if (!f.getType().isPrimitive()) {
	    				f.setAccessible(true);
	    				referenceFields.add(f);
	    			}
	    		}
	    	}
	    }

	    long size = shallowSizeOfInstance(clazz);
	    Object array = Array.newInstance(clazz, 0);
        int arrayBaseOffset = unsafe.arrayBaseOffset(array.getClass());
        int arrayIndexScale = unsafe.arrayIndexScale(array.getClass());	
	    cachedInfo = 
	    	new ClassCache(	alignObjectSize(shallowInstanceSize), 
	    					referenceFields.toArray(new Field[referenceFields.size()]),
	    					size, arrayBaseOffset, arrayIndexScale);
	    return cachedInfo;
	}
	
	private static ClassCache getCacheEntry(final Class<?> clazz) {
		ClassCache cacheEntry = classCache.get(clazz);
		if (cacheEntry == null) {
			cacheEntry = createCacheEntry(clazz);
			classCache.put(clazz, cacheEntry);
		}
	    return cacheEntry;
	}

	private static long adjustForField(long sizeSoFar, final Field f) {
		f.setAccessible(true);
	    final Class<?> type = f.getType();
	    final int fsize = sizeOfType(type);
	    long offsetPlusSize = 0;
		if (Modifier.isStatic(f.getModifiers())) {
			offsetPlusSize = unsafe.staticFieldOffset(f) + fsize;
		}
		else {
			offsetPlusSize = unsafe.objectFieldOffset(f) + fsize;
		}			
		return Math.max(sizeSoFar, offsetPlusSize);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T allocateInstance(Class<T> clazz) {
		try {
			return (T) unsafe.allocateInstance(clazz);
		} 
		catch (InstantiationException e) {
			logger.error("Error at JvmUtil.adjustForField()", e);
			return null;
		}
	}

	public static Class<?> defineClass(byte[] classContents) {
		return unsafe.defineClass(null, classContents, 0, classContents.length);
	}
	
	public static void throwException(Throwable t) {
		unsafe.throwException(t);
	}
    
    public static void info() {
        System.out.println("Running " + (addressSize * BYTE) + "-bit " + options.name + " VM.");
        if (options.compressedRef) {
        	System.out.println("Using compressed references with " + options.compressRefShift + "-bit shift.");
        }
        System.out.println("Objects are " + options.objectAlignment + " bytes aligned.");
        System.out.println();
    }

    private static VMOptions findOptions() {
        // Try Hotspot
        VMOptions hsOpts = getHotspotSpecifics();
        if (hsOpts != null) {
        	return hsOpts;
        }

        // Try JRockit
        VMOptions jrOpts = getJRockitSpecifics();
        if (jrOpts != null) {
        	return jrOpts;
        }
        
        /*
         * When running with CompressedOops on 64-bit platform, the address size
         * reported by Unsafe is still 8, while the real reference fields are 4 bytes long.
         * Try to guess the reference field size with this naive trick.
         */
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
        	switch (oopSize) {
	            case ADDRESSING_8_BYTE:
	            	return new VMOptions("Auto-detected", ADDRESS_SHIFT_SIZE_FOR_BETWEEN_32GB_AND_64_GB);
	            case ADDRESSING_16_BYTE:
	            	return new VMOptions("Auto-detected", ADDRESS_SHIFT_SIZE_FOR_BIGGER_THAN_64_GB);
	            default:
	            	throw new AssertionError("Unsupported address size for compressed reference shifting: " + oopSize); 
        	}    	
        }
        else {
            return new VMOptions("Auto-detected");
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
                    // If compressed oops are enabled, then this option is also accessible
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
        catch (Exception e) {
        	logger.error("Error at JvmUtil.getHotspotSpecifics()", e);
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
        	logger.error("Failed to read JRockit-specific configuration properly", e);
            return null;
        } 
        catch (Exception e) {
        	logger.error("Failed to read JRockit-specific configuration properly", e);
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
    
    @SuppressWarnings("unused")
	private static long addressOf(Object o) {
    	return addressOf(options.referenceSize);
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
            	throw new AssertionError("Unsupported address size: " + oopSize); 
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
    
    private static final class ClassCache {
    	
        final long alignedShallowInstanceSize;
        final Field[] referenceFields;
        final long size;
        final int arrayBaseOffset;
        final int arrayIndexScale;
        
		ClassCache(long alignedShallowInstanceSize, Field[] referenceFields, long size, int arrayBaseOffset, int arrayIndexScale) {
			super();
			this.alignedShallowInstanceSize = alignedShallowInstanceSize;
			this.referenceFields = referenceFields;
			this.size = size;
			this.arrayBaseOffset = arrayBaseOffset;
			this.arrayIndexScale = arrayIndexScale;
		}

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
            this.compressRefShift = 0;
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
