/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import org.apache.log4j.Logger;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class CompressedOopsDetectionUtil {
	
	public static final String DETECTION_SUCCESSFUL = "OK";
	
	private static final Logger logger = Logger.getLogger(CompressedOopsDetectionUtil.class);
	 
	private static Unsafe unsafe;

	private CompressedOopsDetectionUtil() {
        
    }
	
	private static void init() {
		try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
        } 
        catch (NoSuchFieldException e) {
        	throw new RuntimeException("Unable to get unsafe", e);
        } 
        catch (IllegalAccessException e) {
        	throw new RuntimeException("Unable to get unsafe", e);
        }
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	private static <T> long getCompressedAddressByShifting(T obj, int compressOopShift) {
		T[] array = (T[]) new Object[] { obj };
		int baseOffset = unsafe.arrayBaseOffset(Object[].class);
		return normalize(unsafe.getInt(array, baseOffset)) << compressOopShift;
	}
	
	private static long normalize(int value) {
		if (value >= 0) {
			return value;
		}    
		else {
			return (~0L >>> 32) & value;
		}    
	}
	    

	public static void main(String[] args) {
		init();
		int compressOopShift = Integer.parseInt(args[0]);
		CompressedOopTestClass compressedOopTestObj = new CompressedOopTestClass();
		long addressOfObj = 
				getCompressedAddressByShifting(compressedOopTestObj, compressOopShift);
		long l1 = unsafe.getLong(addressOfObj);
		long l2 = unsafe.getLong(compressedOopTestObj, 0L);
		if (l1 == l2) {
			System.out.println(DETECTION_SUCCESSFUL);
		}
	}
	
	private static class CompressedOopTestClass {
		
	}
	
	public static boolean isCompressedOopShiftingThis(int compressOopShift) {
		try {
			Process p = 
					Runtime.getRuntime().exec(
							"java -classpath " + ClasspathUtil.getFullClasspath() + " " + 
							CompressedOopsDetectionUtil.class.getName() + " " + 
							compressOopShift);
	        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        for (String line = br.readLine(); line != null; line = br.readLine()) {
	        	if (DETECTION_SUCCESSFUL.equals(line.trim())) {
	        		return true;
	        	}
	        }
	        p.waitFor();
		} 
		catch (Throwable t) {
			logger.error(
				"Error occured while checking compressed-oop for shifting as " + 
				compressOopShift);
		}
		
		return false;
	}
	
}
