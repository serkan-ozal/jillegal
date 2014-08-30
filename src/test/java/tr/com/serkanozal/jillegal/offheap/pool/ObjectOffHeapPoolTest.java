/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import org.junit.Assert;
import org.junit.Test;

import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.ObjectOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectPoolReferenceType;
import tr.com.serkanozal.jillegal.offheap.pool.impl.EagerReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.LazyReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;

public class ObjectOffHeapPoolTest {

	private static final int ELEMENT_COUNT = 1000;
	
	private OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	
	@Test
	public void objectRetrievedSuccessfullyFromLazyReferencedObjectOffHeapPool() {
		LazyReferencedObjectOffHeapPool<SampleOffHeapClass> objectPool = 
				offHeapService.createOffHeapPool(
						new ObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapClass>().
								type(SampleOffHeapClass.class).
								objectCount(ELEMENT_COUNT).
								referenceType(ObjectPoolReferenceType.LAZY_REFERENCED).
							build());
   
    	for (int i = 0; true; i++) {
    		SampleOffHeapClass obj = objectPool.get();
    		if (obj == null) {
    			break;
    		}
    		Assert.assertEquals(0, obj.getOrder());
    		obj.setOrder(i);
    		obj.setSampleOffHeapAggregatedClass(new SampleOffHeapAggregatedClass());
    		Assert.assertEquals(i, obj.getOrder());
    	}
    	
    	objectPool.reset();
    	
    	for (int i = 0; i < ELEMENT_COUNT; i++) {
    		SampleOffHeapClass obj = objectPool.getAt(i);
    		Assert.assertEquals(0, obj.getOrder());
    		obj.setOrder(i);
    		Assert.assertEquals(i, obj.getOrder());
    	}
	}
	
	@Test
	public void objectRetrievedSuccessfullyFromEagerReferencedObjectOffHeapPool() {
		EagerReferencedObjectOffHeapPool<SampleOffHeapClass> objectPool = 
				offHeapService.createOffHeapPool(
						new ObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapClass>().
								type(SampleOffHeapClass.class).
								objectCount(ELEMENT_COUNT).
								referenceType(ObjectPoolReferenceType.EAGER_REFERENCED).
							build());
   
    	for (int i = 0; true; i++) {
    		SampleOffHeapClass obj = objectPool.get();
    		if (obj == null) {
    			break;
    		}
    		Assert.assertEquals(0, obj.getOrder());
    		obj.setOrder(i);
    		Assert.assertEquals(i, obj.getOrder());
    	}
    	
    	objectPool.reset();
    	
    	for (int i = 0; i < ELEMENT_COUNT; i++) {
    		SampleOffHeapClass obj = objectPool.getAt(i);
    		Assert.assertEquals(0, obj.getOrder());
    		obj.setOrder(i);
    		Assert.assertEquals(i, obj.getOrder());
    	}
	}
	
}
