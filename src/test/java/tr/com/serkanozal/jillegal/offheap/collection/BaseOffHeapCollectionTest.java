/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.collection;

import org.apache.log4j.Logger;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;
import tr.com.serkanozal.jillegal.util.JvmUtil;

@SuppressWarnings("restriction")
public class BaseOffHeapCollectionTest {
	
	protected static final Unsafe UNSAFE = JvmUtil.getUnsafe();
	protected static final long INTEGER_VALUE_FIELD_OFFSET = UNSAFE.objectFieldOffset(JvmUtil.getField(Integer.class, "value"));
	
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
//		if (person.birthDate == null) {
//			person.birthDate = new Date();
//		}
//		person.birthDate.setYear((int) (Math.random() * 100)); // Note that 1900 is added by java.util.Date internally
//		person.birthDate.setMonth((int) (Math.random() * 11));
//		person.birthDate.setDate((int) (1 + Math.random() * 20));
		person.setAccountNo((int) (Math.random() * 1000000));
		person.setDebt(Math.random() * 1000);
		
		return person;
	}

}
