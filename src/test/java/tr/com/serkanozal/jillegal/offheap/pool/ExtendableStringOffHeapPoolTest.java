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

import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.ExtendableStringOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.StringOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.pool.impl.ExtendableStringOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;

public class ExtendableStringOffHeapPoolTest {

	private static final int STRING_COUNT = 1000;
	private static final int TOTAL_STRING_COUNT = 10000;
	private static final int ESTIMATED_STRING_LENGTH = 20;
	
	private OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	
	@Test
	public void stringRetrievedSuccessfullyFromExtendableStringOffHeapPoolWithStringObjectPoolOffHeap() {
		DeeplyForkableStringOffHeapPool stringPool = 
				offHeapService.createOffHeapPool(
						new StringOffHeapPoolCreateParameterBuilder().
								estimatedStringCount(STRING_COUNT).
								estimatedStringLength(ESTIMATED_STRING_LENGTH).
							build());
   
		List<String> strList = new ArrayList<String>();
		
		ExtendableStringOffHeapPool extendableStringPool =
				offHeapService.createOffHeapPool(
						new ExtendableStringOffHeapPoolCreateParameterBuilder().
								forkableStringOffHeapPool(stringPool).
							build());
		
		for (int i = 0; i < TOTAL_STRING_COUNT; i++) {
			String str = extendableStringPool.get("String " + i);
			Assert.assertEquals("String " + i, str);
			strList.add(str);
    	}
		
		for (int i = 0; i < TOTAL_STRING_COUNT; i++) {
			String str = strList.get(i);
			Assert.assertEquals("String " + i, str);
    	}
	}
	
}
