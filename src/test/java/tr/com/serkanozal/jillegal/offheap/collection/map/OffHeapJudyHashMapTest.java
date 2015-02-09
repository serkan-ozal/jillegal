/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.collection.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;

import org.HeapFragger.Idle;
import org.junit.Ignore;
import org.junit.Test;

import tr.com.serkanozal.jillegal.offheap.collection.BaseOffHeapCollectionTest;
import tr.com.serkanozal.jillegal.offheap.collection.Person;
import tr.com.serkanozal.jillegal.util.JvmUtil;

@SuppressWarnings("deprecation")
public class OffHeapJudyHashMapTest extends BaseOffHeapCollectionTest {

	@Test
	public void sizeRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(getOffHeapIntegerKey(1 << i), randomizePerson(i, map.newElement()));
			Assert.assertEquals(i + 1, map.size());
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.remove(1 << i);
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
			map.put(getOffHeapIntegerKey(1 << i), randomizePerson(i, map.newElement()));
		}
		
		Assert.assertFalse(map.isEmpty());
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.remove(1 << i);
		}
		
		Assert.assertTrue(map.isEmpty());
	}
	
	@Test
	public void containsKeyConditionRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(getOffHeapIntegerKey(1 << i), randomizePerson(i, map.newElement()));
			Assert.assertTrue(map.containsKey(1 << i));
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.remove(1 << i);
			Assert.assertFalse(map.containsKey(1 << i));
		}
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void containsValueThrowsUnspportedOperationExceptionAsExpected() {
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		int randomNumber = new Random().nextInt();
		Person randomPerson = randomizePerson(randomNumber, map.newElement());
		
		map.put(getOffHeapIntegerKey(randomNumber), randomPerson);
		
		map.containsValue(randomPerson);
	}
	
	@Test
	public void putGotAndRemovedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(getOffHeapIntegerKey(1 << i), randomizePerson(i, map.newElement()));
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Assert.assertEquals(i, map.get(1 << i).getId());
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.remove(1 << i);
			Assert.assertNull(map.get(1 << i));
		}
	}
	
	@Test
	public void putAllAndRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		Map<Integer, Person> delegatedMap = new HashMap<Integer, Person>();
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			delegatedMap.put(getOffHeapIntegerKey(1 << i), randomizePerson(i, map.newElement()));
		}
		map.putAll(delegatedMap);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Assert.assertEquals(i, map.get(1 << i).getId());
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.remove(1 << i);
			Assert.assertNull(map.get(1 << i));
		}
	}
	
	@Test
	public void clearedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		Assert.assertTrue(map.isEmpty());
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(getOffHeapIntegerKey(1 << i), randomizePerson(i, map.newElement()));
		}
		
		Assert.assertFalse(map.isEmpty());
		
		map.clear();
		
		Assert.assertTrue(map.isEmpty());
		
		Assert.assertEquals(0, map.size());
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Assert.assertNull(map.get(1 << i));
		}
	}
	
	@Test
	public void keySetRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(getOffHeapIntegerKey(1 << i), randomizePerson(i, map.newElement()));
		}
		
		int i = 0;
		for (Integer key : map.keySet()) {
			Integer expectedKey = 1 << i;
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
			map.put(getOffHeapIntegerKey(1 << i), randomizePerson(i, map.newElement()));
		}
		
		int i = 0;
		for (Person value : map.values()) {
			Assert.assertEquals(i, value.getId());
			i++;
		}
		Assert.assertEquals(ENTRY_COUNT, i);
	}
	
	@Test
	public void entrySetRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashMap<Integer, Person> map = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(getOffHeapIntegerKey(1 << i), randomizePerson(i, map.newElement()));
		}
		
		int i = 0;
		for (Map.Entry<Integer, Person> entry : map.entrySet()) {
			Assert.assertEquals((Integer)(1 << i), entry.getKey());
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
			judyMap.put(getOffHeapIntegerKey(i), randomizePerson(i, judyMap.newElement()));
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
			judyMap.put(getOffHeapIntegerKey(i), randomizePerson(i, judyMap.newElement()));
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
			judyMap.put(getOffHeapIntegerKey(i), randomizePerson(i, judyMap.newElement()));
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
		final int ENTRY_COUNT = 100000;
		
		OffHeapJudyHashMap<Integer, Person> judyMap = 
				new OffHeapJudyHashMap<Integer, Person>(Person.class);
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.put(getOffHeapIntegerKey(i), randomizePerson(i, judyMap.newElement()));
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
	
	/**
	 * This test is ignored by default since it is load test.
	 */
	@Ignore
	@Test
	// -XX:-UseCompressedOops -XX:+UseSerialGC        -verbose:gc -XX:+PrintGCDetails -XX:+PrintPromotionFailure -javaagent:C:\Users\%USERPROFILE%\.m2\repository\org\giltene\HeapFragger\1.0\HeapFragger-1.0.jar="-a 400 -s 512"
	// -XX:-UseCompressedOops -XX:+UseSerialGC        -verbose:gc -XX:+PrintGCDetails -XX:+PrintPromotionFailure -javaagent:~/.m2/repository/org/giltene/HeapFragger/1.0/HeapFragger-1.0.jar="-a 400 -s 512"
	
	// -XX:-UseCompressedOops -XX:+UseConcMarkSweepGC -verbose:gc -XX:+PrintGCDetails -XX:+PrintPromotionFailure -javaagent:C:\Users\%USERPROFILE%\.m2\repository\org\giltene\HeapFragger\1.0\HeapFragger-1.0.jar="-a 400 -s 512"
	// -XX:-UseCompressedOops -XX:+UseConcMarkSweepGC -verbose:gc -XX:+PrintGCDetails -XX:+PrintPromotionFailure -javaagent:~/.m2/repository/org/giltene/HeapFragger/1.0/HeapFragger-1.0.jar="-a 400 -s 512"
	
	// -XX:-UseCompressedOops -XX:+UseG1GC            -verbose:gc -XX:+PrintGCDetails -XX:+PrintPromotionFailure -javaagent:C:\Users\%USERPROFILE%\.m2\repository\org\giltene\HeapFragger\1.0\HeapFragger-1.0.jar="-a 400 -s 512"
	// -XX:-UseCompressedOops -XX:+UseG1GC            -verbose:gc -XX:+PrintGCDetails -XX:+PrintPromotionFailure -javaagent:~/.m2/repository/org/giltene/HeapFragger/1.0/HeapFragger-1.0.jar="-a 400 -s 512"
		
	// Note that: Crashes with "-XX:+UseParallelGC"
	// -XX:-UseCompressedOops -XX:+UseParallelGC      -verbose:gc -XX:+PrintGCDetails -XX:+PrintPromotionFailure -javaagent:C:\Users\%USERPROFILE%\.m2\repository\org\giltene\HeapFragger\1.0\HeapFragger-1.0.jar="-a 400 -s 512"
	// -XX:-UseCompressedOops -XX:+UseParallelGC      -verbose:gc -XX:+PrintGCDetails -XX:+PrintPromotionFailure -javaagent:~/.m2/repository/org/giltene/HeapFragger/1.0/HeapFragger-1.0.jar="-a 400 -s 512"
	public void stressTest() {
		Thread t = new Thread() {
			public void run() {
				Idle.main(new String[] {"-t", "1000000000"});
			};
		};
		t.start();
		
		final int ENTRY_COUNT = 1000000;
		final int ITERATION_COUNT = 100;

		OffHeapJudyHashMap<Integer, Person> judyMap = 
				new OffHeapJudyHashMap<Integer, Person>(Integer.class, Person.class);

		System.out.println("********** ROUND    1 **********");
		System.out.println("********************************");
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.put(getOffHeapIntegerKey(i), randomizePerson(i, judyMap.newElement())); // They will be added
		}
		
		System.out.println("********************************");
		
		JvmUtil.runGC();

		for (int i = 0; i < ENTRY_COUNT; i++) {
			Person p = judyMap.get(i);
			if (i % 1000 == 0) {
				System.out.println(p);
			}	
		}
		
		JvmUtil.runGC();
		
		List<Person> list = new ArrayList<Person>();
		
		// We must sure that there is no out of memory.
		// Because new puts will be replaced with old ones and they will be free.
		// So there must not be memory leak and out of memory error.
		for (int i = 1; i < ITERATION_COUNT; i++) {
			System.out.printf("********** ROUND %4d **********\n", (i + 1));
			System.out.println("********************************");
			
			for (int j = 0; j < ENTRY_COUNT; j++) {
				Person person = randomizePerson(j, judyMap.newElement());
				Person replaced = 
					judyMap.put(getOffHeapIntegerKey(j), person); // They will be replaced
				// OffHeap map only disposes only keys, not values.
				// So disposing elements is developer's responsibility.
				if (replaced != null) {
					offHeapService.freeObject(replaced);
				}
				list.add(person);
			}
			
			JvmUtil.runGC();
			
			list.clear();
			
			JvmUtil.runGC();
			
			try {
				Thread.sleep(10000); // Wait for 10 seconds
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			} 
			
			JvmUtil.runGC();
			
			System.out.println("********************************");
		}
		
		JvmUtil.runGC();
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Person p = judyMap.get(i);
			if (i % 1000 == 0) {
				System.out.println(p);
			}	
		}
		
		JvmUtil.runGC();
	}
	
	@Ignore
	@Test
	public void multiThreadStressTest() throws InterruptedException {
		final int PUT_COUNT_IN_A_ITERATION = 1000;
		final int REMOVE_COUNT_IN_A_ITERATION = 10;
		final int GET_COUNT_IN_A_ITERATION = 1000;
		final int ENTRY_COUNT = 1000000;
		final int ITERATION_COUNT = 100;
		
		final Random random = new Random();
		List<Person> list = new ArrayList<Person>();

		OffHeapJudyHashMap<Long, Person> judyMap = 
				new OffHeapJudyHashMap<Long, Person>(Long.class, Person.class);

		for (int i = 0; i < ENTRY_COUNT; i++) {
			Person p = randomizePerson(i, judyMap.newElement());
			judyMap.put(offHeapService.getOffHeapLong(i), p); // They will be added
			list.add(p);
			Assert.assertEquals("Username-" + i, p.getUsername());
			Assert.assertEquals("Firstname-" + i, p.getFirstName());
			Assert.assertEquals("Lastname-" + i, p.getLastName());
		}
		
		JvmUtil.runGC();
		
		list.clear();
		
		Thread[] threads = new Thread[10];
		for (int k = 0; k < threads.length; k++) {
			Thread t = new Thread() {
				public void run() {
					for (int i = 0; i < ITERATION_COUNT; i++) {
						long start = System.currentTimeMillis();
						System.out.println("Iteration-" + i + " started at thread " +
								Thread.currentThread().getName() + " ...");
						
						for (int j = 0; j < PUT_COUNT_IN_A_ITERATION; j++) {
							int id = random.nextInt(ENTRY_COUNT);
							Person replacedPerson = 
									judyMap.put(offHeapService.getOffHeapLong(id), 
												randomizePerson(id, judyMap.newElement()));
							if (replacedPerson != null) {
								offHeapService.freeObject(replacedPerson);
								// Since Person object itself is "OffHeapAwareObject", 
								// its itself frees its username, first name and last name
							}
						}	
						
						JvmUtil.runGC();

						for (int j = 0; j < REMOVE_COUNT_IN_A_ITERATION; j++) {
							int id = random.nextInt(ENTRY_COUNT);
							Person removedPerson = judyMap.remove(id);
							if (removedPerson != null) {
								offHeapService.freeObject(removedPerson);
								// Since Person object itself is "OffHeapAwareObject", 
								// its itself frees its username, first name and last name
							}
						}
						
						JvmUtil.runGC();

						for (int j = 0; j < GET_COUNT_IN_A_ITERATION; j++) {
							int id = random.nextInt(ENTRY_COUNT);
							Person p = judyMap.get(id);
							if (p != null) {
								Assert.assertEquals(id, p.getId());
								Assert.assertEquals("Username-" + id, p.getUsername());
								Assert.assertEquals("Firstname-" + id, p.getFirstName());
								Assert.assertEquals("Lastname-" + id, p.getLastName());
							}	
						}
						
						JvmUtil.runGC();
						
						long finish = System.currentTimeMillis();
						System.out.println("Iteration-" + i + " finished at thread " + 
								Thread.currentThread().getName() + " in " + (finish - start) + " milliseconds");
						
					}
				};
			};
			threads[k] = t;
			t.start();
		}	
		
		for (Thread t : threads) {
			t.join();
		}
	}

}
