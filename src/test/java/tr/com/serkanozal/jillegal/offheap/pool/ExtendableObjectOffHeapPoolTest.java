/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.DefaultExtendableObjectOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.ExtendableObjectOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.ObjectOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectPoolReferenceType;
import tr.com.serkanozal.jillegal.offheap.pool.impl.EagerReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.ExtendableObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.LazyReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;

public class ExtendableObjectOffHeapPoolTest {

	private static final int ELEMENT_COUNT = 100;
	private static final int TOTAL_ELEMENT_COUNT = 1000;
	
	private OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	
	@Test
	public void objectRetrievedSuccessfullyFromExtendableObjectOffHeapPoolWithLazyReferencedSequentialObjectPoolOffHeap() {
		LazyReferencedObjectOffHeapPool<SampleOffHeapClass> sequentialObjectPool = 
				offHeapService.createOffHeapPool(
						new ObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapClass>().
								type(SampleOffHeapClass.class).
								objectCount(ELEMENT_COUNT).
								referenceType(ObjectPoolReferenceType.LAZY_REFERENCED).
							build());
   
		ExtendableObjectOffHeapPool<SampleOffHeapClass> extendableObjectPool =
				offHeapService.createOffHeapPool(
						new ExtendableObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapClass>().
								forkableObjectOffHeapPool(sequentialObjectPool).
							build());
		
		List<SampleOffHeapClass> objList = new ArrayList<SampleOffHeapClass>();
		
    	for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
    		SampleOffHeapClass obj = extendableObjectPool.get();
    		obj.setOrder(i);
    		objList.add(obj);
    	}
    	
    	for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
    		SampleOffHeapClass obj = objList.get(i);
    		Assert.assertEquals(i, obj.getOrder());
    	}
	}
	
	@Test
	public void objectRetrievedSuccessfullyFromExtendableObjectOffHeapPoolWithEagerReferencedSequentialObjectPoolOffHeap() {
		EagerReferencedObjectOffHeapPool<SampleOffHeapClass> sequentialObjectPool = 
				offHeapService.createOffHeapPool(
						new ObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapClass>().
								type(SampleOffHeapClass.class).
								objectCount(ELEMENT_COUNT).
								referenceType(ObjectPoolReferenceType.EAGER_REFERENCED).
							build());
   
		ExtendableObjectOffHeapPool<SampleOffHeapClass> extendableObjectPool =
				offHeapService.createOffHeapPool(
						new ExtendableObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapClass>().
								forkableObjectOffHeapPool(sequentialObjectPool).
							build());
		
		List<SampleOffHeapClass> objList = new ArrayList<SampleOffHeapClass>();
		
    	for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
    		SampleOffHeapClass obj = extendableObjectPool.get();
    		obj.setOrder(i);
    		objList.add(obj);
    	}
    	
    	for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
    		SampleOffHeapClass obj = objList.get(i);
    		Assert.assertEquals(i, obj.getOrder());
    	}
	}
	
	@Test
	public void objectRetrievedSuccessfullyFromExtendableObjectOffHeapPoolWithDefaultObjectOffHeapPool() {
		ExtendableObjectOffHeapPool<SampleOffHeapClass> extendableObjectPool =
				offHeapService.createOffHeapPool(
						new DefaultExtendableObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapClass>().
								elementType(SampleOffHeapClass.class).
							build());
		
		List<SampleOffHeapClass> objList = new ArrayList<SampleOffHeapClass>();
		
    	for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
    		SampleOffHeapClass obj = extendableObjectPool.get();
    		obj.setOrder(i);
    		objList.add(obj);
    	}
    	
    	for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
    		SampleOffHeapClass obj = objList.get(i);
    		Assert.assertEquals(i, obj.getOrder());
    	}
	}
	
}
