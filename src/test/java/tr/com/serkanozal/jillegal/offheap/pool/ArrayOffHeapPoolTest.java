/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import junit.framework.Assert;

import org.junit.Test;

import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.ArrayOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.pool.impl.ComplexTypeArrayOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.PrimitiveTypeArrayOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;

@SuppressWarnings("deprecation")
public class ArrayOffHeapPoolTest {

	private static final int ELEMENT_COUNT = 1000;
	
	private OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	
	@Test
	public void complexTypeArrayOffHeapPoolSuccessfullyCreatedWithNoInitializationAndUsed() {
		ComplexTypeArrayOffHeapPool<SampleOffHeapPoolTestClass> complexTypeArrayPool = 
				offHeapService.createOffHeapPool(
						new ArrayOffHeapPoolCreateParameterBuilder<SampleOffHeapPoolTestClass>().
								type(SampleOffHeapPoolTestClass.class).
								length(ELEMENT_COUNT).
								initializeElements(false).
							build());
		
		SampleOffHeapPoolTestClass[] array = complexTypeArrayPool.getArray();
		
    	for (int i = 0; i < ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = new SampleOffHeapPoolTestClass();
    		obj.setOrder(i);
    		complexTypeArrayPool.setAt(obj, i); // Note that "array[i] = obj" is not valid, because JVM doesn't know array created at off-heap
    	}
    	
    	for (int i = 0; i < ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = array[i];
    		Assert.assertEquals(i, obj.getOrder());
    	}
	}
	
	@Test
	public void complexTypeArrayOffHeapPoolSuccessfullyCreatedWithInitializationAndUsed() {
		ComplexTypeArrayOffHeapPool<SampleOffHeapPoolTestClass> complexTypeArrayPool = 
				offHeapService.createOffHeapPool(
						new ArrayOffHeapPoolCreateParameterBuilder<SampleOffHeapPoolTestClass>().
								type(SampleOffHeapPoolTestClass.class).
								length(ELEMENT_COUNT).
								initializeElements(true).
							build());
		
		SampleOffHeapPoolTestClass[] array = complexTypeArrayPool.getArray();
		
    	for (int i = 0; i < ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = array[i];
    		obj.setOrder(i);
    	}
    	
    	for (int i = 0; i < ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = array[i];
    		Assert.assertEquals(i, obj.getOrder());
    	}
	}
	
	@Test
	public void primitiveTypeArrayOffHeapPoolSuccessfullyCreatedAndUsed() {
		PrimitiveTypeArrayOffHeapPool<Integer, int[]> primitiveTypeArrayPool = 
				offHeapService.createOffHeapPool(
						new ArrayOffHeapPoolCreateParameterBuilder<Integer>().
								type(Integer.class).
								length(ELEMENT_COUNT).
								usePrimitiveTypes(true).
							build());
		
		int[] array = primitiveTypeArrayPool.getArray();
		
    	for (int i = 0; i < ELEMENT_COUNT; i++) {
    		array[i] = i;
    	}
    	
    	for (int i = 0; i < ELEMENT_COUNT; i++) {
    		int number = array[i];
    		Assert.assertEquals(i, number);
    	}
	}
		
}
