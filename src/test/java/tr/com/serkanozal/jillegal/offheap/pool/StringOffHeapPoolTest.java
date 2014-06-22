/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import org.junit.Test;

import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.StringOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;

public class StringOffHeapPoolTest {

	private static final int STRING_COUNT = 1000;
	
	private OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	
	@Test
	public void stringsRetrievedSuccessfullyStringOffHeapPool() {
		StringOffHeapPool stringPool = 
				offHeapService.createOffHeapPool(
						new StringOffHeapPoolCreateParameterBuilder().
								estimatedStringCount(STRING_COUNT).
								estimatedStringLength(20).
							build());
   
    	for (int i = 0; i < STRING_COUNT; i++) {
    		System.out.println(stringPool.get("String " + i));
    	}
	}
	
}
