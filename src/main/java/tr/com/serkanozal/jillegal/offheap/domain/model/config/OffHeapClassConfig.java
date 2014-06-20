/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.domain.model.config;

import java.util.List;

import tr.com.serkanozal.jillegal.domain.model.Mergeable;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;

public class OffHeapClassConfig implements OffHeapConfig, Mergeable<OffHeapClassConfig> {
	
	protected Class<?> clazz;
	protected NonPrimitiveFieldAllocationConfigType nonPrimitiveFieldAllocationConfigType;
	protected List<OffHeapObjectFieldConfig> objectFieldConfigs;
	protected List<OffHeapArrayFieldConfig> arrayFieldConfigs;
	
	public Class<?> getClazz() {
		return clazz;
	}
	
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	public NonPrimitiveFieldAllocationConfigType getNonPrimitiveFieldAllocationConfigType() {
		return nonPrimitiveFieldAllocationConfigType;
	}
	
	public void setNonPrimitiveFieldAllocationConfigType(
			NonPrimitiveFieldAllocationConfigType nonPrimitiveFieldAllocationConfigType) {
		this.nonPrimitiveFieldAllocationConfigType = nonPrimitiveFieldAllocationConfigType;
	}
	
	public List<OffHeapObjectFieldConfig> getObjectFieldConfigs() {
		return objectFieldConfigs;
	}
	
	public void setObjectFieldConfigs(List<OffHeapObjectFieldConfig> objectFieldConfigs) {
		this.objectFieldConfigs = objectFieldConfigs;
	}
	
	public List<OffHeapArrayFieldConfig> getArrayFieldConfigs() {
		return arrayFieldConfigs;
	}
	
	public void setArrayFieldConfigs(List<OffHeapArrayFieldConfig> arrayFieldConfigs) {
		this.arrayFieldConfigs = arrayFieldConfigs;
	}
	
	@Override
	public OffHeapClassConfig merge(OffHeapClassConfig config) {
		if (clazz == null) {
			clazz = config.clazz;
		}
		if (config.objectFieldConfigs != null && !config.objectFieldConfigs.isEmpty()) {
			if (objectFieldConfigs == null || objectFieldConfigs.isEmpty()) {
				objectFieldConfigs = config.objectFieldConfigs;
			}
			else {
				for (OffHeapObjectFieldConfig objectFieldConfig : config.objectFieldConfigs) {
					if (!objectFieldConfigs.contains(objectFieldConfig)) {
						objectFieldConfigs.add(objectFieldConfig);
					}
				}
			}
		}	
		if (config.arrayFieldConfigs != null && !config.arrayFieldConfigs.isEmpty()) {
			if (arrayFieldConfigs == null || arrayFieldConfigs.isEmpty()) {
				arrayFieldConfigs = config.arrayFieldConfigs;
			}
			else {
				for (OffHeapArrayFieldConfig arrayFieldConfig : config.arrayFieldConfigs) {
					if (!arrayFieldConfigs.contains(arrayFieldConfig)) {
						arrayFieldConfigs.add(arrayFieldConfig);
					}
				}
			}
		}	
		return this;
	}
	
}
