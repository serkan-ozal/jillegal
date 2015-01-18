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
	
	protected Integer getOffHeapIntegerKey(int key) {
		return offHeapService.getOffHeapInteger(key);
	}
	
	protected Person randomizePerson(int key, Person person) {
		person.setId(key);
		person.setUsername(offHeapService.newString("Username-" + key));
		person.setFirstName(offHeapService.newString("Firstname-" + key));
		person.setLastName(offHeapService.newString("Lastname-" + key));
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
