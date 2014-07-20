/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.pool;

import tr.com.serkanozal.jillegal.offheap.service.OffHeapConstants;

public class StringOffHeapPoolCreateParameter implements OffHeapPoolCreateParameter<String> {

	protected int estimatedStringCount = OffHeapConstants.DEFAULT_ESTIMATED_STRING_COUNT;
	protected int estimatedStringLength = OffHeapConstants.DEFAULT_ESTIMATED_STRING_LENGTH;
	
	public StringOffHeapPoolCreateParameter() {
		
	}
	
	public StringOffHeapPoolCreateParameter(int estimatedStringCount,
			int estimatedStringLength) {
		this.estimatedStringCount = estimatedStringCount;
		this.estimatedStringLength = estimatedStringLength;
	}

	@Override
	public OffHeapPoolType getOffHeapPoolType() {
		return OffHeapPoolType.STRING_POOL;
	}
	
	@Override
	public Class<String> getElementType() {
		return String.class;
	}
	
	@Override
	public boolean isMakeOffHeapableAsAuto() {
		return false;
	}
	
	public int getEstimatedStringCount() {
		return estimatedStringCount;
	}
	
	public void setEstimatedStringCount(int estimatedStringCount) {
		this.estimatedStringCount = estimatedStringCount;
	}
	
	public int getEstimatedStringLength() {
		return estimatedStringLength;
	}
	
	public void setEstimatedStringLength(int estimatedStringLength) {
		this.estimatedStringLength = estimatedStringLength;
	}

}
