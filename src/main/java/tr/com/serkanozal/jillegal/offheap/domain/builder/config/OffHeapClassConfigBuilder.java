/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.builder.config;

import java.util.ArrayList;
import java.util.List;

import tr.com.serkanozal.jillegal.domain.builder.Builder;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapArrayFieldConfig;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapClassConfig;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapObjectFieldConfig;

public class OffHeapClassConfigBuilder implements Builder<OffHeapClassConfig> {

	private Class<?> clazz;
	private List<OffHeapObjectFieldConfig> objectFieldConfigs;
	private List<OffHeapArrayFieldConfig> arrayFieldConfigs;
	
	public OffHeapClassConfigBuilder clazz(Class<?> clazz) {
		this.clazz = clazz;
		return this;
	}
	
	public OffHeapClassConfigBuilder objectFieldConfigs(List<OffHeapObjectFieldConfig> objectFieldConfigs) {
		this.objectFieldConfigs = objectFieldConfigs;
		return this;
	}
	
	public OffHeapClassConfigBuilder addObjectFieldConfig(OffHeapObjectFieldConfig objectFieldConfig) {
		if (objectFieldConfigs == null) {
			objectFieldConfigs = new ArrayList<OffHeapObjectFieldConfig>();
		}
		objectFieldConfigs.add(objectFieldConfig);
		return this;
	}
	
	public OffHeapClassConfigBuilder arrayFieldConfigs(List<OffHeapArrayFieldConfig> arrayFieldConfigs) {
		this.arrayFieldConfigs = arrayFieldConfigs;
		return this;
	}
	
	public OffHeapClassConfigBuilder addArrayFieldConfig(OffHeapArrayFieldConfig arrayFieldConfig) {
		if (arrayFieldConfigs == null) {
			arrayFieldConfigs = new ArrayList<OffHeapArrayFieldConfig>();
		}
		arrayFieldConfigs.add(arrayFieldConfig);
		return this;
	}
	
	@Override
	public OffHeapClassConfig build() {
		OffHeapClassConfig config = new OffHeapClassConfig();
		config.setClazz(clazz);
		config.setObjectFieldConfigs(objectFieldConfigs);
		config.setArrayFieldConfigs(arrayFieldConfigs);
		return config;
	}
	
}
