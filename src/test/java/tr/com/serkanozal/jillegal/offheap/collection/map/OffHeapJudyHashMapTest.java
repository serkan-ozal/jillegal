/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.collection.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;

import org.junit.Test;

import tr.com.serkanozal.jillegal.util.JvmUtil;

@SuppressWarnings("deprecation")
public class OffHeapJudyHashMapTest {

	@Test
	public void sizeRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		Map<Integer, String> map = new OffHeapJudyHashMap<Integer, String>();
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, i + " << " + i);
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
		Map<Integer, String> map = new OffHeapJudyHashMap<Integer, String>();
		
		Assert.assertTrue(map.isEmpty());
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, i + " << " + i);
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
		Map<Integer, String> map = new OffHeapJudyHashMap<Integer, String>();
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, i + " << " + i);
			Assert.assertTrue(map.containsKey(i << i));
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.remove(i << i);
			Assert.assertFalse(map.containsKey(i << i));
		}
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void containsValueThrowsUnspportedOperationExceptionAsExpected() {
		Map<Integer, String> map = new OffHeapJudyHashMap<Integer, String>();
		
		int randomNumber = new Random().nextInt();
		
		map.put(randomNumber, "Random number is " + randomNumber);
		
		map.containsValue("Random number is " + randomNumber);
	}
	
	@Test
	public void putGotAndRemovedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		Map<Integer, String> map = new OffHeapJudyHashMap<Integer, String>();
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, i + " << " + i);
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Assert.assertEquals(i + " << " + i, map.get(i << i));
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.remove(i << i);
			Assert.assertNull(map.get(i << i));
		}
	}
	
	@Test
	public void putAllAndRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		Map<Integer, String> delegatedMap = new HashMap<Integer, String>();
		Map<Integer, String> map = new OffHeapJudyHashMap<Integer, String>();
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			delegatedMap.put(i << i, i + " << " + i);
		}
		map.putAll(delegatedMap);
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			Assert.assertEquals(i + " << " + i, map.get(i << i));
		}
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.remove(i << i);
			Assert.assertNull(map.get(i << i));
		}
	}
	
	@Test
	public void clearedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		Map<Integer, String> map = new OffHeapJudyHashMap<Integer, String>();
		
		Assert.assertTrue(map.isEmpty());
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, i + " << " + i);
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
		Map<Integer, String> map = new OffHeapJudyHashMap<Integer, String>();
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, i + " << " + i);
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
		Map<Integer, String> map = new OffHeapJudyHashMap<Integer, String>();
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, i + " << " + i);
		}
		
		int i = 0;
		for (String value : map.values()) {
			String expectedValue = i + " << " + i;
			i++;
			Assert.assertEquals(expectedValue, value);
		}
	}
	
	@Test
	public void entrySetRetrievedSuccessfully() {
		final int ENTRY_COUNT = Integer.SIZE - 1;
		Map<Integer, String> map = new OffHeapJudyHashMap<Integer, String>();
		
		for (int i = 0; i < ENTRY_COUNT; i++) {
			map.put(i << i, i + " << " + i);
		}
		
		int i = 0;
		for (Map.Entry<Integer, String> entry : map.entrySet()) {
			Integer expectedKey = i << i;
			String expectedValue = i + " << " + i;
			i++;
			Assert.assertEquals(expectedKey, entry.getKey());
			Assert.assertEquals(expectedValue, entry.getValue());
		}
	}
	
	@Test
	public void judyHashMapVsHashMapForPutOperation() {
		final int ENTRY_COUNT = 1000000;
		
		Map<Integer, String> judyMap = new OffHeapJudyHashMap<Integer, String>();
		long judyMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.put(i, String.valueOf(i));
		}
		long judyMapFinishTime = System.nanoTime();
		long judyMapExecutionTime = judyMapFinishTime - judyMapStartTime;
		
		JvmUtil.runGC();
		
		try {
			Thread.sleep(3000); // Wait a few seconds
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		} 
		
		Map<Integer, String> hashMap = new HashMap<Integer, String>();
		long hashMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			hashMap.put(i, String.valueOf(i));
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
		final int ENTRY_COUNT = 1000000;
		
		Map<Integer, String> judyMap = new OffHeapJudyHashMap<Integer, String>();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.put(i, String.valueOf(i));
		}
		long judyMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.get(i);
		}
		long judyMapFinishTime = System.nanoTime();
		long judyMapExecutionTime = judyMapFinishTime - judyMapStartTime;
		
		JvmUtil.runGC();
		
		try {
			Thread.sleep(3000); // Wait a few seconds
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		} 
		
		Map<Integer, String> hashMap = new HashMap<Integer, String>();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			hashMap.put(i, String.valueOf(i));
		}
		long hashMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			hashMap.get(i);
		}
		long hashMapFinishTime = System.nanoTime();
		long hashMapExecutionTime = hashMapFinishTime - hashMapStartTime;
		
		System.out.println("Judy Map Execution Time for " + ENTRY_COUNT + 
							" get operation: " + (judyMapExecutionTime / 1000) + " milliseconds ...");
		System.out.println("Hash Map Execution Time for " + ENTRY_COUNT + 
							" get operation: " + (hashMapExecutionTime / 1000) + " milliseconds ...");
	}
	
	@Test
	public void judyHashMapVsConcurrentHashMapForPutOperation() {
		final int ENTRY_COUNT = 1000000;
		
		Map<Integer, String> judyMap = new OffHeapJudyHashMap<Integer, String>();
		long judyMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.put(i, String.valueOf(i));
		}
		long judyMapFinishTime = System.nanoTime();
		long judyMapExecutionTime = judyMapFinishTime - judyMapStartTime;
		
		JvmUtil.runGC();
		
		try {
			Thread.sleep(3000); // Wait a few seconds
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		} 
		
		Map<Integer, String> concurrentHashMap = new ConcurrentHashMap<Integer, String>();
		long concurrentHashMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			concurrentHashMap.put(i, String.valueOf(i));
		}
		long concurrentHashMapFinishTime = System.nanoTime();
		long concurrentHashMapExecutionTime = concurrentHashMapFinishTime - concurrentHashMapStartTime;
		
		System.out.println("Judy Map Execution Time for " + ENTRY_COUNT + 
							" put operation: " + (judyMapExecutionTime / 1000) + " milliseconds ...");
		System.out.println("Concurrent Hash Map Execution Time for " + ENTRY_COUNT + 
							" put operation: " + (concurrentHashMapExecutionTime / 1000) + " milliseconds ...");
	}
	
	@Test
	public void judyHashMapVsConcurrentHashMapForGetOperation() {
		final int ENTRY_COUNT = 1000000;
		
		Map<Integer, String> judyMap = new OffHeapJudyHashMap<Integer, String>();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.put(i, String.valueOf(i));
		}
		long judyMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			judyMap.get(i);
		}
		long judyMapFinishTime = System.nanoTime();
		long judyMapExecutionTime = judyMapFinishTime - judyMapStartTime;
		
		JvmUtil.runGC();
		
		try {
			Thread.sleep(3000); // Wait a few seconds
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		} 
		
		Map<Integer, String> concurrentHashMap = new ConcurrentHashMap<Integer, String>();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			concurrentHashMap.put(i, String.valueOf(i));
		}
		long concurrentHashMapStartTime = System.nanoTime();
		for (int i = 0; i < ENTRY_COUNT; i++) {
			concurrentHashMap.get(i);
		}
		long concurrentHashMapFinishTime = System.nanoTime();
		long concurrentHashMapExecutionTime = concurrentHashMapFinishTime - concurrentHashMapStartTime;
		
		System.out.println("Judy Map Execution Time for " + ENTRY_COUNT + 
							" get operation: " + (judyMapExecutionTime / 1000) + " milliseconds ...");
		System.out.println("Concurrent Hash Map Execution Time for " + ENTRY_COUNT + 
							" get operation: " + (concurrentHashMapExecutionTime / 1000) + " milliseconds ...");
	}
	
}
