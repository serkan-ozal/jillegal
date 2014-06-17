/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.config;

import tr.com.serkanozal.jillegal.config.BaseConfigService;
import tr.com.serkanozal.jillegal.instrument.config.provider.InstrumentConfigProvider;
import tr.com.serkanozal.jillegal.instrument.config.provider.annotation.AnnotationBasedInstrumentConfigProvider;

public class InstrumentConfigServiceImpl extends BaseConfigService<InstrumentConfigProvider> implements InstrumentConfigService {

	@Override
	protected void init() {
		registerConfigProvider(new AnnotationBasedInstrumentConfigProvider());
	}
	
}
