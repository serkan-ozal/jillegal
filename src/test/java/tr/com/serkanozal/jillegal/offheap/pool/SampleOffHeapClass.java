/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool;

import tr.com.serkanozal.jillegal.offheap.config.provider.annotation.OffHeapObject;

public class SampleOffHeapClass {

	private int i;
	private long l;
	private int order;
	@OffHeapObject
	private SampleOffHeapAggregatedClass sampleOffHeapAggregatedClass;
	
	public int getI() {
		return i;
	}
	
	public void setI(int i) {
		this.i = i;
	}
	
	public long getL() {
		return l;
	}
	
	public void setL(long l) {
		this.l = l;
	}
	
	public int getOrder() {
		return order;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
	
	public SampleOffHeapAggregatedClass getSampleOffHeapAggregatedClass() {
		return sampleOffHeapAggregatedClass;
	}
	
	public void setSampleOffHeapAggregatedClass(SampleOffHeapAggregatedClass sampleOffHeapAggregatedClass) {
		this.sampleOffHeapAggregatedClass = sampleOffHeapAggregatedClass;
	}
	
}
