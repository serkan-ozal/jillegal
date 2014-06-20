/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import tr.com.serkanozal.jillegal.config.annotation.JillegalAware;
import tr.com.serkanozal.jillegal.offheap.config.provider.annotation.OffHeapArray;

@JillegalAware
public class SampleOffHeapWrapperClass {

	@OffHeapArray(length = 100000)
	private SampleOffHeapClass[] sampleOffHeapClassArray;
	
	public SampleOffHeapClass[] getSampleOffHeapClassArray() {
		return sampleOffHeapClassArray;
	}
	
	public void setSampleOffHeapClassArray(SampleOffHeapClass[] sampleOffHeapClassArray) {
		this.sampleOffHeapClassArray = sampleOffHeapClassArray;
	}	
	
}
