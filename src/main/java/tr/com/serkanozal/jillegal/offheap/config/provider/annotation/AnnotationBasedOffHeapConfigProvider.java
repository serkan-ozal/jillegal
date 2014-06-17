/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.config.provider.annotation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jcommon.util.ReflectionUtil;
import tr.com.serkanozal.jillegal.offheap.config.provider.OffHeapConfigProvider;
import tr.com.serkanozal.jillegal.offheap.domain.builder.config.OffHeapArrayFieldConfigBuilder;
import tr.com.serkanozal.jillegal.offheap.domain.builder.config.OffHeapClassConfigBuilder;
import tr.com.serkanozal.jillegal.offheap.domain.builder.config.OffHeapObjectFieldConfigBuilder;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapClassConfig;

public class AnnotationBasedOffHeapConfigProvider implements OffHeapConfigProvider {

	protected final Logger logger = Logger.getLogger(getClass());
	
	protected Map<Class<?>, OffHeapClassConfig> classConfigMap = new HashMap<Class<?>, OffHeapClassConfig>();
	
	@Override
	public OffHeapClassConfig getOffHeapClassConfig(Class<?> clazz) {
		OffHeapClassConfig classConfig = classConfigMap.get(clazz);
		if (classConfig == null) {
			classConfig = findOffHeapClassConfig(clazz);
			classConfigMap.put(clazz, classConfig);
		}
		return classConfig;
	}
	
	protected OffHeapClassConfig findOffHeapClassConfig(Class<?> clazz) {
		List<Field> fields = ReflectionUtil.getAllFields(clazz, AllocateAtOffHeap.class);
		OffHeapClassConfigBuilder classConfigBuilder = 
				new OffHeapClassConfigBuilder().
						clazz(clazz);
		for (Field f : fields) {
			OffHeapObject offHeapObject = f.getAnnotation(OffHeapObject.class);
			if (offHeapObject != null) {
				if (!f.getType().isArray()) {
					classConfigBuilder.addObjectFieldConfig(
							new OffHeapObjectFieldConfigBuilder().
									field(f).
									fieldType(offHeapObject.fieldType()).
								build());
				}
				else {
					logger.error("@OffHeapObject is not for array typed fields !");
				}
			}
			OffHeapArray offHeapArray = f.getAnnotation(OffHeapArray.class);
			if (offHeapArray != null) {
				if (f.getType().isArray()) {
					if (offHeapArray.length() <= 0) {
						logger.error("\"length\" attribute of @OffHeapArray is must be positive number !");
					}
					else {
						classConfigBuilder.addArrayFieldConfig(
								new OffHeapArrayFieldConfigBuilder().
										field(f).
										elementType(offHeapArray.elementType()).
										length(offHeapArray.length()).
									build());
					}	
				}
				else {
					logger.error("@OffHeapArray is only for array typed fields !");
				}
			}
		}
		return classConfigBuilder.build();
	}

}
