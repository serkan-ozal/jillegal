/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.pool;

import junit.framework.Assert;

import org.junit.Test;

@SuppressWarnings("deprecation")
public class SequentialObjectPoolTest {

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
	public void objectRetrievedFromSequentialObjectPool() {
		final int OBJECT_COUNT = 10000;
		
		SequentialObjectPool<SampleClass> sequentialObjectPool = 
    			new SequentialObjectPool<SampleClass>(SampleClass.class, OBJECT_COUNT);
   
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
