/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import junit.framework.Assert;

import org.junit.Test;

import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.SequentialObjectOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectPoolReferenceType;
import tr.com.serkanozal.jillegal.offheap.pool.impl.EagerReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.LazyReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;

@SuppressWarnings("deprecation")
public class SequentialObjectOffHeapPoolTest {

	private static final int ELEMENT_COUNT = 1000;
	
	private OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	
	@Test
	public void objectRetrievedSuccessfullyFromLazyReferencedSequentialObjectOffHeapPool() {
		LazyReferencedObjectOffHeapPool<SampleOffHeapPoolTestClass> sequentialObjectPool = 
				offHeapService.createOffHeapPool(
						new SequentialObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapPoolTestClass>().
								type(SampleOffHeapPoolTestClass.class).
								objectCount(ELEMENT_COUNT).
								referenceType(ObjectPoolReferenceType.LAZY_REFERENCED).
							build());
   
    	for (int i = 0; i < ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = sequentialObjectPool.get();
    		obj.setOrder(i);
    	}
    	
    	for (int i = 0; i < ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = sequentialObjectPool.getAt(i);
    		Assert.assertEquals(i, obj.getOrder());
    	}
	}
	
	@Test
	public void objectRetrievedSuccessfullyFromEagerReferencedSequentialObjectOffHeapPool() {
		EagerReferencedObjectOffHeapPool<SampleOffHeapPoolTestClass> sequentialObjectPool = 
				offHeapService.createOffHeapPool(
						new SequentialObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapPoolTestClass>().
								type(SampleOffHeapPoolTestClass.class).
								objectCount(ELEMENT_COUNT).
								referenceType(ObjectPoolReferenceType.EAGER_REFERENCED).
							build());
   
    	for (int i = 0; i < ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = sequentialObjectPool.get();
    		obj.setOrder(i);
    	}
    	
    	for (int i = 0; i < ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = sequentialObjectPool.getAt(i);
    		Assert.assertEquals(i, obj.getOrder());
    	}
	}
	
}
