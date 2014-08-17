/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.instance;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.instance.StringInstanceRequest;

public class StringInstanceRequestBuilder implements Builder<StringInstanceRequest> {

	private String str;
	
	@Override
	public StringInstanceRequest build() {
		return new StringInstanceRequest(str);
	}
	
	public StringInstanceRequestBuilder string(String str) {
		this.str = str;
		return this;
	}

}
