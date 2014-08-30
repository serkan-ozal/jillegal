/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import org.junit.Assert;
import org.junit.Test;

import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.StringOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;

public class StringOffHeapPoolTest {

	private static final int STRING_COUNT = 1000;
	private static final int ESTIMATED_STRING_LENGTH = 20;
	
	private OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	
	@Test
	public void stringsRetrievedSuccessfullyStringOffHeapPool() {
		StringOffHeapPool stringPool = 
				offHeapService.createOffHeapPool(
						new StringOffHeapPoolCreateParameterBuilder().
								estimatedStringCount(STRING_COUNT).
								estimatedStringLength(ESTIMATED_STRING_LENGTH).
							build());
   
    	for (int i = 0; true; i++) {
    		String str = stringPool.get("String " + i);
    		if (str == null) {
    			break;
    		}
    		Assert.assertEquals("String " + i, str);
    	}
    	
    	stringPool.reset();
    	
    	for (int i = 0; true; i++) {
    		String str = stringPool.get("String " + (STRING_COUNT + i));
    		if (str == null) {
    			break;
    		}
    		Assert.assertEquals("String " + (STRING_COUNT + i), str);
    	}
	}
	
}
