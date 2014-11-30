/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.collection.map;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;

import tr.com.serkanozal.jillegal.offheap.config.provider.annotation.OffHeapObject;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;

@SuppressWarnings("deprecation")
public class OffHeapJudyHashMapTest {

	private static final Logger logger = Logger.getLogger(OffHeapJudyHashMapTest.class);
	
	private static final OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	
	@Test
	public void sizeRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, randomizePerson(i, map.newElement()));
			Assert.assertEquals(i + 1, map.size());
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.remove(i << i);
			Assert.assertEquals(ENTRY_COUNT - (i + 1), map.size());
		}
	}
	
	
	@Test
	public void isEmptyConditionRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		Assert.assertTrue(map.isEmpty());
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, randomizePerson(i, map.newElement()));
		}
		
		Assert.assertFalse(map.isEmpty());
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.remove(i << i);
		}
		
		Assert.assertTrue(map.isEmpty());
	}
	
	@Test
	public void containsKeyConditionRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, randomizePerson(i, map.newElement()));
			Assert.assertTrue(map.containsKey(i << i));
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.remove(i << i);
			Assert.assertFalse(map.containsKey(i << i));
		}
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void containsValueThrowsUnspportedOperationExceptionAsExpected() {
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		int randomNumber = new Random().nextInt();
		Person randomPerson = randomizePerson(randomNumber, map.newElement());
		
		map.put(randomNumber, randomPerson);
		
		map.containsValue(randomPerson);
	}
	
	@Test
	public void putGotAndRemovedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, randomizePerson(i, map.newElement()));
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Assert.assertEquals(i, map.get(i << i).getId());
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.remove(i << i);
			Assert.assertNull(map.get(i << i));
		}
	}
	
	@Test
	public void putAllAndRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		Map<Integer, Person> delegatedMap = new HashMap<Integer, Person>();
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			delegatedMap.put(i << i, randomizePerson(i, map.newElement()));
		}
		map.putAll(delegatedMap);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Assert.assertEquals(i, map.get(i << i).getId());
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.remove(i << i);
			Assert.assertNull(map.get(i << i));
		}
	}
	
	@Test
	public void clearedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		Assert.assertTrue(map.isEmpty());
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, randomizePerson(i, map.newElement()));
		}
		
		Assert.assertFalse(map.isEmpty());
		
		map.clear();
		
		Assert.assertTrue(map.isEmpty());
		
		Assert.assertEquals(0, map.size());
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Assert.assertNull(map.get(i << i));
		}
	}
	
	@Test
	public void keySetRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, randomizePerson(i, map.newElement()));
		}
		
		int i = 0;
		for (Integer key : map.keySet()) {
			Integer expectedKey = i << i;
			i++;
			Assert.assertEquals(expectedKey, key);
		}
	}
	
	@Test
	public void valuesRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, randomizePerson(i, map.newElement()));
		}
		
		int i = 0;
		for (Person value : map.values()) {
			Assert.assertEquals(i, value.getId());
			i++;
		}
	}
	
	@Test
	public void entrySetRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, randomizePerson(i, map.newElement()));
		}
		
		int i = 0;
		for (Map.Entry<Integer, Person> entry : map.entrySet()) {
			Assert.assertEquals((Integer)(i << i), entry.getKey());
			Assert.assertEquals(i, entry.getValue().getId());
			i++;
		}
	}
	
	@Test
	public void judyHashMapVsHashMapForPutOperation() {
		final int ENTRY_COUNT = 100000;
		
		OffHeapJudyHashMap<Integer, Person> judyMap = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		long judyMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.put(i, randomizePerson(i, judyMap.newElement()));
		}
		long judyMapFinishTime = System.nanoTime();
		long judyMapExecutionTime = judyMapFinishTime - judyMapStartTime;
		
		Map<Integer, Person> hashMap = new HashMap<Integer, Person>();
		long hashMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			hashMap.put(i, randomizePerson(i, new Person()));
		}
		long hashMapFinishTime = System.nanoTime();
		long hashMapExecutionTime = hashMapFinishTime - hashMapStartTime;
		
		System.out.println("Judy Map Execution Time for " + ENTRY_COUNT + 
							" put operation: " + (judyMapExecutionTime / 1000) + " milliseconds ...");
		System.out.println("Hash Map Execution Time for " + ENTRY_COUNT + 
							" put operation: " + (hashMapExecutionTime / 1000) + " milliseconds ...");
	}
	
	@Test
	public void judyHashMapVsHashMapForGetOperation() {
		final int ENTRY_COUNT = 100000;
		
		OffHeapJudyHashMap<Integer, Person> judyMap = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.put(i, randomizePerson(i, judyMap.newElement()));
		}

		long judyMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.get(i);
		}
		long judyMapFinishTime = System.nanoTime();
		long judyMapExecutionTime = judyMapFinishTime - judyMapStartTime;
		
		Map<Integer, Person> hashMap = new HashMap<Integer, Person>();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			hashMap.put(i, randomizePerson(i, new Person()));
		}

		long hashMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			hashMap.get(i);
		}
		long hashMapFinishTime = System.nanoTime();
		long hashMapExecutionTime = hashMapFinishTime - hashMapStartTime;
		
		logger.info("Judy Map Execution Time for " + ENTRY_COUNT + 
							" get operation: " + (judyMapExecutionTime / 1000) + " milliseconds ...");
		logger.info("Hash Map Execution Time for " + ENTRY_COUNT + 
							" get operation: " + (hashMapExecutionTime / 1000) + " milliseconds ...");
	}
	
	@Test
	public void judyHashMapVsConcurrentHashMapForPutOperation() {
		final int ENTRY_COUNT = 100000;
		
		OffHeapJudyHashMap<Integer, Person> judyMap = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		long judyMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.put(i, randomizePerson(i, judyMap.newElement()));
		}
		long judyMapFinishTime = System.nanoTime();
		long judyMapExecutionTime = judyMapFinishTime - judyMapStartTime;
		
		Map<Integer, Person> concurrentHashMap = new ConcurrentHashMap<Integer, Person>();
		long concurrentHashMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			concurrentHashMap.put(i, randomizePerson(i, new Person()));
		}
		long concurrentHashMapFinishTime = System.nanoTime();
		long concurrentHashMapExecutionTime = concurrentHashMapFinishTime - concurrentHashMapStartTime;
		
		logger.info("Judy Map Execution Time for " + ENTRY_COUNT + 
							" put operation: " + (judyMapExecutionTime / 1000) + " milliseconds ...");
		logger.info("Concurrent Hash Map Execution Time for " + ENTRY_COUNT + 
							" put operation: " + (concurrentHashMapExecutionTime / 1000) + " milliseconds ...");
	}
	
	@Test
	public void judyHashMapVsConcurrentHashMapForGetOperation() {
		final int ENTRY_COUNT = 1000000;
		
		OffHeapJudyHashMap<Integer, Person> judyMap = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.put(i, randomizePerson(i, judyMap.newElement()));
		}

		long judyMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.get(i);
		}
		long judyMapFinishTime = System.nanoTime();
		long judyMapExecutionTime = judyMapFinishTime - judyMapStartTime;
		
		Map<Integer, Person> concurrentHashMap = new ConcurrentHashMap<Integer, Person>();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			concurrentHashMap.put(i, randomizePerson(i, new Person()));
		}

		long concurrentHashMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			concurrentHashMap.get(i);
		}
		long concurrentHashMapFinishTime = System.nanoTime();
		long concurrentHashMapExecutionTime = concurrentHashMapFinishTime - concurrentHashMapStartTime;
		
		logger.info("Judy Map Execution Time for " + ENTRY_COUNT + 
							" get operation: " + (judyMapExecutionTime / 1000) + " milliseconds ...");
		logger.info("Concurrent Hash Map Execution Time for " + ENTRY_COUNT + 
							" get operation: " + (concurrentHashMapExecutionTime / 1000) + " milliseconds ...");
	}
	
	private static Person randomizePerson(int key, Person person) {
		person.id = key;
		person.setUsername(offHeapService.newString("Username-" + key));
		person.setFirstName(offHeapService.newString("Firstname-" + key));
		person.setLastName(offHeapService.newString("Lastname-" + key));
		if (person.birthDate == null) {
			person.birthDate = new Date();
		}
		person.birthDate.setYear((int) (Math.random() * 100)); // Note that 1900 is added by java.util.Date internally
		person.birthDate.setMonth((int) (Math.random() * 11));
		person.birthDate.setDate((int) (1 + Math.random() * 20));
		person.accountNo = (int) (Math.random() * 1000000);
		person.debt = Math.random() * 1000;
		return person;
	}
	
	public static class Person {
		
		private long id;
		private String username;
		private String firstName;
		private String lastName;
		@OffHeapObject
		private Date birthDate;
		private int accountNo;
		private double debt;
		
		public Person() {
			
		}
		
		public Person(long id, String username, String firstName,
				String lastName, Date birthDate, int accountNo, double debt) {
			this.id = id;
			this.username = username;
			this.firstName = firstName;
			this.lastName = lastName;
			this.birthDate = birthDate;
			this.accountNo = accountNo;
			this.debt = debt;
		}

		public long getId() {
			return id;
		}
		
		public void setId(long id) {
			this.id = id;
		}
		
		public String getUsername() {
			return username;
		}
		
		public void setUsername(String username) {
			this.username = username;
		}
		
		public String getFirstName() {
			return firstName;
		}
		
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		
		public String getLastName() {
			return lastName;
		}
		
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
		
		public Date getBirthDate() {
			return birthDate;
		}
		
		public void setBirthDate(Date birthDate) {
			this.birthDate = birthDate;
		}
		
		public int getAccountNo() {
			return accountNo;
		}
		
		public void setAccountNo(int accountNo) {
			this.accountNo = accountNo;
		}
		
		public double getDebt() {
			return debt;
		}
		
		public void setDebt(double debt) {
			this.debt = debt;
		}

		@Override
		public String toString() {
			return "Person [id=" + id + ", username=" + username
					+ ", firstName=" + firstName + ", lastName=" + lastName
					+ ", birthDate=" + birthDate + ", accountNo=" + accountNo
					+ ", debt=" + debt + "]";
		}
	
	}
	
}
