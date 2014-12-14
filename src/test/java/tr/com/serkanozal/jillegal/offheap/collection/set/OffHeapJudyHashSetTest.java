/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.collection.set;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import tr.com.serkanozal.jillegal.offheap.collection.BaseOffHeapCollectionTest;
import tr.com.serkanozal.jillegal.offheap.collection.Person;
import tr.com.serkanozal.jillegal.util.JvmUtil;

@SuppressWarnings("deprecation")
public class OffHeapJudyHashSetTest extends BaseOffHeapCollectionTest {

	@Test
	public void sizeRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		List<Person> elements = new ArrayList<Person>();
		OffHeapJudyHashSet<Person> set = 
				new OffHeapJudyHashSet<Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Person person = set.newElement();
			set.add(randomizePerson(i, person));
			elements.add(person);
			Assert.assertEquals(i + 1, set.size());
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			set.remove(elements.get(i));
			Assert.assertEquals(ENTRY_COUNT - (i + 1), set.size());
		}
	}
	
	@Test
	public void isEmptyConditionRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		List<Person> elements = new ArrayList<Person>();
		OffHeapJudyHashSet<Person> set = 
				new OffHeapJudyHashSet<Person>(Person.class);
		
		Assert.assertTrue(set.isEmpty());
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Person person = set.newElement();
			set.add(randomizePerson(i, person));
			elements.add(person);
		}
		
		Assert.assertFalse(set.isEmpty());
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			set.remove(elements.get(i));
		}
		
		Assert.assertTrue(set.isEmpty());
	}
	
	@Test
	public void containsConditionRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		List<Person> elements = new ArrayList<Person>();
		OffHeapJudyHashSet<Person> set = 
				new OffHeapJudyHashSet<Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Person person = set.newElement();
			set.add(randomizePerson(i, person));
			elements.add(person);
			Assert.assertTrue(set.contains(person));
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Person person = elements.get(i);
			set.remove(person);
			Assert.assertFalse(set.contains(person));
		}
	}
	
	@Test
	public void addGotAndRemovedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		OffHeapJudyHashSet<Person> set = 
				new OffHeapJudyHashSet<Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Person person = set.newElement();
			set.add(randomizePerson(i, person));
			Assert.assertEquals(i + 1, set.size());
		}
		
		int i = 0;
		for (Person person : set) {
			Assert.assertEquals(i++, person.getId());
		}
		
		int j = ENTRY_COUNT;
		for (Person person : set) {
			set.remove(person);
			Assert.assertEquals(--j, set.size());
			Assert.assertFalse(set.contains(person));
		}
	}
	
	@SuppressWarnings("unused")
	@Test
	public void addAllAndRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		Set<Person> delegatedSet = new HashSet<Person>();
		OffHeapJudyHashSet<Person> set = 
				new OffHeapJudyHashSet<Person>(Person.class);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			delegatedSet.add(randomizePerson(i, set.newElement()));
		}
		set.addAll(delegatedSet);
		
		Assert.assertEquals(ENTRY_COUNT, set.size());
		
		int elementCount = 0;
		for (Person person : set) {
			elementCount++;
		}
		Assert.assertEquals(ENTRY_COUNT, elementCount);
	}
	
	@Test
	public void clearedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		List<Person> elements = new ArrayList<Person>();
		OffHeapJudyHashSet<Person> set = 
				new OffHeapJudyHashSet<Person>(Person.class);
		
		Assert.assertTrue(set.isEmpty());
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Person person = set.newElement();
			set.add(randomizePerson(i, person));
			elements.add(person);
		}
		
		Assert.assertFalse(set.isEmpty());
		
		set.clear();
		
		Assert.assertTrue(set.isEmpty());
		
		Assert.assertEquals(0, set.size());
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Assert.assertFalse(set.contains(elements.get(i)));
		}
	}
	
	@Test
	public void judyHashSetVsHashSetForAddOperation() {
		final int ENTRY_COUNT = 100000;
		
		OffHeapJudyHashSet<Person> judySet = 
				new OffHeapJudyHashSet<Person>(Person.class);
		long judySetStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judySet.add(randomizePerson(i, judySet.newElement()));
		}
		long judySetFinishTime = System.nanoTime();
		long judySetExecutionTime = judySetFinishTime - judySetStartTime;
		
		Set<Person> hashSet = new HashSet<Person>();
		long hashSetStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			hashSet.add(randomizePerson(i, new Person()));
		}
		long hashSetFinishTime = System.nanoTime();
		long hashSetExecutionTime = hashSetFinishTime - hashSetStartTime;
		
		System.out.println("Judy Set Execution Time for " + ENTRY_COUNT + 
							" add operation: " + (judySetExecutionTime / 1000) + " milliseconds ...");
		System.out.println("Hash Set Execution Time for " + ENTRY_COUNT + 
							" add operation: " + (hashSetExecutionTime / 1000) + " milliseconds ...");
	}
	
	@SuppressWarnings("unused")
	@Test
	public void judyHashSetVsHashSetForIterateOperation() {
		final int ENTRY_COUNT = 100000;
		
		OffHeapJudyHashSet<Person> judySet = 
				new OffHeapJudyHashSet<Person>(Person.class);
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judySet.add(randomizePerson(i, judySet.newElement()));
		}

		long judySetStartTime = System.nanoTime();
		long judySetTotalElementCount = 0;
		for (Person person : judySet) {
			judySetTotalElementCount++;
		}
		long judySetFinishTime = System.nanoTime();
		long judySetExecutionTime = judySetFinishTime - judySetStartTime;
		Assert.assertEquals(ENTRY_COUNT, judySetTotalElementCount);
		
		Set<Person> hashSet = new HashSet<Person>();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			hashSet.add(randomizePerson(i, new Person()));
		}

		long hashSetStartTime = System.nanoTime();
		long hashSetTotalElementCount = 0;
		for (Person person : hashSet) {
			hashSetTotalElementCount++;
		}
		long hashSetFinishTime = System.nanoTime();
		long hashSetExecutionTime = hashSetFinishTime - hashSetStartTime;
		Assert.assertEquals(ENTRY_COUNT, hashSetTotalElementCount);
		
		logger.info("Judy Set Execution Time for " + ENTRY_COUNT + 
							" iterate operation: " + (judySetExecutionTime / 1000) + " milliseconds ...");
		logger.info("Hash Set Execution Time for " + ENTRY_COUNT + 
							" iterate operation: " + (hashSetExecutionTime / 1000) + " milliseconds ...");
	}
	
	/**
	 * This test is ignored by default since it is load test.
	 */
	@Test
	@Ignore
	public void stressTest() {
		final int ENTRY_COUNT = 1000000;
		final int ITERATION_COUNT = 100;

		OffHeapJudyHashSet<Person> judySet = 
				new OffHeapJudyHashSet<Person>(Person.class);
		
		System.out.println("********** ROUND    1 **********");
		System.out.println("********************************");
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judySet.add(randomizePerson(i, judySet.newElement())); // They will be added
		}
		
		System.out.println("********************************");
		
		JvmUtil.runGC();

		Iterator<Person> iter = judySet.iterator();
		for (int i = 0; i < ENTRY_COUNT && iter.hasNext(); i++) {
			Person p = iter.next();
			if (i % 1000 == 0) {
				System.out.println(p);
			}	
		}
		
		JvmUtil.runGC();
		
		// We must sure that there is no out of memory.
		// Because new puts will be replaced with old ones and they will be free.
		// So there must not be memory leak and out of memory error.
		for (int i = 1; i < ITERATION_COUNT; i++) {
			System.out.printf("********** ROUND %4d **********\n", (i + 1));
			System.out.println("********************************");
			
			for (int j = 0; j < ENTRY_COUNT && iter.hasNext(); j++) {
				Person replaced = 
					judySet.put(randomizePerson(j, judySet.newElement())); // They will be replaced
				// OffHeap set doesn't dispose elements.
				// So disposing elements is developer's responsibility.
				if (replaced != null) {
					// If there is an old value, at first dispose its elements and dispose it's itself.
					offHeapService.freeString(replaced.getUsername());
					offHeapService.freeString(replaced.getFirstName());
					offHeapService.freeString(replaced.getLastName());
					offHeapService.freeObject(replaced);
				}
			}
			
			try {
				Thread.sleep(10000); // Wait for 10 seconds
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			} 
			
			System.out.println("********************************");
		}
		
		JvmUtil.runGC();
		
		iter = judySet.iterator();
		for (int i = 0; i < ENTRY_COUNT && iter.hasNext(); i++) {
			Person p = iter.next();
			if (i % 1000 == 0) {
				System.out.println(p);
			}	
		}
		
		JvmUtil.runGC();
	}
	
}
