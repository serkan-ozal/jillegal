/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.collection;

import java.util.Random;

import org.apache.log4j.Logger;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;
import tr.com.serkanozal.jillegal.util.JvmUtil;

@SuppressWarnings("restriction")
public class BaseOffHeapCollectionTest {
	
	protected static final Unsafe UNSAFE = JvmUtil.getUnsafe();
	protected static final long INTEGER_VALUE_FIELD_OFFSET = UNSAFE.objectFieldOffset(JvmUtil.getField(Integer.class, "value"));
	protected static final Random RANDOM = new Random();
	
	protected final OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	protected final Logger logger = Logger.getLogger(getClass());
	
	protected static final char[] DIGIT_ONES = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	};
	
	protected static final char[] DIGIT_TENS = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
	};

	protected final CharArrayThreadLocal USERNAME_CHAR_ARRAY_BUFFER = new CharArrayThreadLocal("Username-");
	protected final CharArrayThreadLocal FIRSTNAME_CHAR_ARRAY_BUFFER = new CharArrayThreadLocal("Firstname-");
	protected final CharArrayThreadLocal LASTNAME_CHAR_ARRAY_BUFFER = new CharArrayThreadLocal("Lastname-");
	
	
	protected class CharArray {
		
		private char[] chars;
		private int actualLength;
		
		private CharArray(String prefix, int maxLength) {
			this.chars = new char[maxLength];
			this.actualLength = prefix.length();
			for (int i = 0; i < prefix.length(); i++) {
				chars[i] = prefix.charAt(i);
			}
		}
		
	}
	
	protected class CharArrayThreadLocal extends ThreadLocal<CharArray> {
		
		private static final int DEFAULT_MAX_LENGTH = 30;
		
		private final String prefix;
		private final int maxLength;
		
		private CharArrayThreadLocal(String prefix) {
			this(prefix, DEFAULT_MAX_LENGTH);
		}
		
		private CharArrayThreadLocal(String prefix, int maxLength) {
			this.prefix = prefix;
			this.maxLength = maxLength;
		}
		
		@Override
		protected CharArray initialValue() {
			return new CharArray(prefix, maxLength);
		}
		
		private CharArray getFor(long l) {  
	        int size = (l < 0) ? stringSize(-l) + 1 : stringSize(l);
	        int offset = prefix.length();
	        CharArray charArray = get();
	        getChars(l, size, charArray.chars, offset);
	        charArray.actualLength = size + offset;
	        return charArray;
	    }
		
		private void getChars(long i, int index, char[] buf, int offset) {
	        long q;
	        int r;
	        int charPos = index;
	        char sign = 0;

	        if (i < 0) {
	            sign = '-';
	            i = -i;
	        }

	        // Get 2 digits/iteration using longs until quotient fits into an int
	        while (i > Integer.MAX_VALUE) {
	            q = i / 100;
	            // really: r = i - (q * 100);
	            r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
	            i = q;
	            buf[offset + --charPos] = DIGIT_ONES[r];
	            buf[offset + --charPos] = DIGIT_TENS[r];
	        }

	        // Get 2 digits/iteration using ints
	        int q2;
	        int i2 = (int)i;
	        while (i2 >= 65536) {
	            q2 = i2 / 100;
	            // really: r = i2 - (q * 100);
	            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
	            i2 = q2;
	            buf[offset + --charPos] = DIGIT_ONES[r];
	            buf[offset + --charPos] = DIGIT_TENS[r];
	        }

	        // Fall thru to fast mode for smaller numbers
	        // assert(i2 <= 65536, i2);
	        for (;;) {
	            q2 = (i2 * 52429) >>> (16+3);
	            r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
	            buf[offset + --charPos] = (char) ('0' + r);
	            i2 = q2;
	            if (i2 == 0) break;
	        }
	        if (sign != 0) {
	            buf[offset + --charPos] = sign;
	        }
	    }
		
		// Requires positive x
	    private int stringSize(long x) {
	        long p = 10;
	        for (int i = 1; i < 19; i++) {
	            if (x < p) {
	                return i;
	            }    
	            p = 10 * p;
	        }
	        return 19;
	    }
		
	}
	
	
	protected Integer getOffHeapIntegerKey(int key) {
		return offHeapService.getOffHeapInteger(key);
	}
	
	protected Person randomizePerson(int key, Person person) {
		person.setId(key);
		//person.setUsername(offHeapService.newString("Username-" + key));
		//person.setFirstName(offHeapService.newString("Firstname-" + key));
		//person.setLastName(offHeapService.newString("Lastname-" + key));
		CharArray usernameCharArray = USERNAME_CHAR_ARRAY_BUFFER.getFor(key);
		person.setUsername(offHeapService.newString(usernameCharArray.chars, 0, usernameCharArray.actualLength));
		//person.setUsername(offHeapService.newString("Username-" + key));
		CharArray firstNameCharArray = FIRSTNAME_CHAR_ARRAY_BUFFER.getFor(key);
		person.setFirstName(offHeapService.newString(firstNameCharArray.chars, 0, firstNameCharArray.actualLength));
		//person.setFirstName(offHeapService.newString("Firstname-" + key));
		CharArray lastNameCharArray = LASTNAME_CHAR_ARRAY_BUFFER.getFor(key);
		person.setLastName(offHeapService.newString(lastNameCharArray.chars, 0, lastNameCharArray.actualLength));
		person.setBirthDate(
				(Person.MILLI_SECONDS_IN_A_YEAR * RANDOM.nextInt(30)) + 	// Any year between 1970 and 2000
				(Person.MILLI_SECONDS_IN_A_MONTH * (RANDOM.nextInt(12))) +	// Any month between 0 and 11 (Jan and Dec)
				(Person.MILLI_SECONDS_IN_A_DAY * (RANDOM.nextInt(29)))); 	// Any day between 0 and 28
//		if (person.birthDate == null) {
//			person.birthDate = new Date();
//		}
//		person.birthDate.setYear((int) (RANDOM.nextInt(100))); // Note that 1900 is added by java.util.Date internally
//		person.birthDate.setMonth((int) (RANDOM.nextInt(12));
//		person.birthDate.setDate((int) (1 + RANDOM.nextInt(28)));
		person.setAccountNo((int) (RANDOM.nextInt(1000000)));
		person.setDebt(RANDOM.nextInt(1000));
		
		return person;
	}

}
