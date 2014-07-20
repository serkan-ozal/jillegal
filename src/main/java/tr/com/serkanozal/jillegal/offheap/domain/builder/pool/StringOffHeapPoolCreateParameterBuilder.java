/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.pool;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.StringOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapConstants;

public class StringOffHeapPoolCreateParameterBuilder implements Builder<StringOffHeapPoolCreateParameter> {

	private int estimatedStringCount = OffHeapConstants.DEFAULT_ESTIMATED_STRING_COUNT;
	private int estimatedStringLength = OffHeapConstants.DEFAULT_ESTIMATED_STRING_LENGTH;
	
	@Override
	public StringOffHeapPoolCreateParameter build() {
		return 
			new StringOffHeapPoolCreateParameter(
					estimatedStringCount, 
					estimatedStringLength);
	}
	
	public StringOffHeapPoolCreateParameterBuilder estimatedStringCount(int estimatedStringCount) {
		this.estimatedStringCount = estimatedStringCount;
		return this;
	}
	
	public StringOffHeapPoolCreateParameterBuilder estimatedStringLength(int estimatedStringLength) {
		this.estimatedStringLength = estimatedStringLength;
		return this;
	}

}
