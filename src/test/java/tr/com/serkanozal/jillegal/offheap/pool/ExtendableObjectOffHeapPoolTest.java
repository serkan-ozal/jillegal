/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.DefaultExtendableObjectOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.ExtendableObjectOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.SequentialObjectOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectPoolReferenceType;
import tr.com.serkanozal.jillegal.offheap.pool.impl.EagerReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.ExtendableObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.LazyReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;

@SuppressWarnings("deprecation")
public class ExtendableObjectOffHeapPoolTest {

	private static final int ELEMENT_COUNT = 100;
	private static final int TOTAL_ELEMENT_COUNT = 1000;
	
	private OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	
	@Test
	public void objectRetrievedSuccessfullyFromExtendableObjectOffHeapPoolWithLazyReferencedSequentialObjectPoolOffHeap() {
		LazyReferencedObjectOffHeapPool<SampleOffHeapPoolTestClass> sequentialObjectPool = 
				offHeapService.createOffHeapPool(
						new SequentialObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapPoolTestClass>().
								type(SampleOffHeapPoolTestClass.class).
								objectCount(ELEMENT_COUNT).
								referenceType(ObjectPoolReferenceType.LAZY_REFERENCED).
							build());
   
		ExtendableObjectOffHeapPool<SampleOffHeapPoolTestClass> extendableObjectPool =
				offHeapService.createOffHeapPool(
						new ExtendableObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapPoolTestClass>().
								forkableObjectOffHeapPool(sequentialObjectPool).
							build());
		
		List<SampleOffHeapPoolTestClass> objList = new ArrayList<SampleOffHeapPoolTestClass>();
		
    	for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = extendableObjectPool.get();
    		obj.setOrder(i);
    		objList.add(obj);
    	}
    	
    	for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = objList.get(i);
    		Assert.assertEquals(i, obj.getOrder());
    	}
	}
	
	@Test
	public void objectRetrievedSuccessfullyFromExtendableObjectOffHeapPoolWithEagerReferencedSequentialObjectPoolOffHeap() {
		EagerReferencedObjectOffHeapPool<SampleOffHeapPoolTestClass> sequentialObjectPool = 
				offHeapService.createOffHeapPool(
						new SequentialObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapPoolTestClass>().
								type(SampleOffHeapPoolTestClass.class).
								objectCount(ELEMENT_COUNT).
								referenceType(ObjectPoolReferenceType.EAGER_REFERENCED).
							build());
   
		ExtendableObjectOffHeapPool<SampleOffHeapPoolTestClass> extendableObjectPool =
				offHeapService.createOffHeapPool(
						new ExtendableObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapPoolTestClass>().
								forkableObjectOffHeapPool(sequentialObjectPool).
							build());
		
		List<SampleOffHeapPoolTestClass> objList = new ArrayList<SampleOffHeapPoolTestClass>();
		
    	for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = extendableObjectPool.get();
    		obj.setOrder(i);
    		objList.add(obj);
    	}
    	
    	for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = objList.get(i);
    		Assert.assertEquals(i, obj.getOrder());
    	}
	}
	
	@Test
	public void objectRetrievedSuccessfullyFromExtendableObjectOffHeapPoolWithDefaultObjectOffHeapPool() {
		ExtendableObjectOffHeapPool<SampleOffHeapPoolTestClass> extendableObjectPool =
				offHeapService.createOffHeapPool(
						new DefaultExtendableObjectOffHeapPoolCreateParameterBuilder<SampleOffHeapPoolTestClass>().
								elementType(SampleOffHeapPoolTestClass.class).
							build());
		
		List<SampleOffHeapPoolTestClass> objList = new ArrayList<SampleOffHeapPoolTestClass>();
		
    	for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = extendableObjectPool.get();
    		obj.setOrder(i);
    		objList.add(obj);
    	}
    	
    	for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
    		SampleOffHeapPoolTestClass obj = objList.get(i);
    		Assert.assertEquals(i, obj.getOrder());
    	}
	}
	
}
