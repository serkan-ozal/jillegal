/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import junit.framework.Assert;

import org.junit.Test;

import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.SequentialObjectPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.pool.SequentialObjectPool;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;

@SuppressWarnings("deprecation")
public class SequentialObjectPoolTest {

	private OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	
	public static class SampleClass {
		
		private int i1 = 5;
		private int i2 = 10;
		private int order;
		
		public int getI1() {
			return i1;
		}
		
		public int getI2() {
			return i2;
		}
		
		public int getOrder() {
			return order;
		}
		
		public void setOrder(int order) {
			this.order = order;
		}
		
	}
	
	@Test
	public void objectRetrievedSuccessfullyFromSequentialObjectPool() {
		final int OBJECT_COUNT = 10000;
		
		SequentialObjectPool<SampleClass> sequentialObjectPool = 
				offHeapService.createOffHeapPool(
						new SequentialObjectPoolCreateParameterBuilder<SampleClass>().
								type(SampleClass.class).
								objectCount(OBJECT_COUNT).
							build()
				);
   
    	for (int i = 0; i < OBJECT_COUNT; i++) {
    		SampleClass obj = sequentialObjectPool.newObject();
    		obj.setOrder(i);
    	}
    	for (int i = 0; i < OBJECT_COUNT; i++) {
    		SampleClass obj = sequentialObjectPool.getObject(i);
    		Assert.assertEquals(i, obj.getOrder());
    	}
	}
	
}
