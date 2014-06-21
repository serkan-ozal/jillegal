/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jillegal.config.ConfigManager;
import tr.com.serkanozal.jillegal.offheap.config.OffHeapConfigService;
import tr.com.serkanozal.jillegal.offheap.domain.model.config.OffHeapFieldConfig;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.NonPrimitiveFieldAllocationConfigType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;
import tr.com.serkanozal.jillegal.offheap.util.OffHeapUtil;
import tr.com.serkanozal.jillegal.offheap.util.OffHeapUtil.NonPrimitiveFieldInitializer;

public abstract class BaseOffHeapPool<T, P extends OffHeapPoolCreateParameter<T>> implements OffHeapPool<T, P> {

	protected final Logger logger = Logger.getLogger(getClass());
	
	protected static final ThreadLocal<Set<Class<?>>> classesInProcessStore = new ThreadLocal<Set<Class<?>>>();
	
	protected Class<T> elementType;
	protected DirectMemoryService directMemoryService;
	protected NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType;
	protected Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> nonPrimitiveFieldInitializers;
	protected OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	protected OffHeapConfigService offHeapConfigService = ConfigManager.getOffHeapConfigService();
	protected long allocationStartAddress;
	protected long allocationEndAddress;
	protected long allocationSize;
	
	public BaseOffHeapPool(Class<T> elementType) {
		if (elementType == null) {
			throw new IllegalArgumentException("\"elementType\" cannot be null !");
		}
		this.elementType = elementType;
		this.directMemoryService = DirectMemoryServiceFactory.getDirectMemoryService();
	}
	
	public BaseOffHeapPool(Class<T> elementType, DirectMemoryService directMemoryService) {
		if (elementType == null) {
			throw new IllegalArgumentException("\"elementType\" cannot be null !");
		}
		this.elementType = elementType;
		this.directMemoryService = 
				directMemoryService != null ? 
						directMemoryService : 
						DirectMemoryServiceFactory.getDirectMemoryService();
	}
	
	protected void init(Class<T> elementType, 
			NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType, 
			DirectMemoryService directMemoryService) {
		this.allocateNonPrimitiveFieldsAtOffHeapConfigType = allocateNonPrimitiveFieldsAtOffHeapConfigType;
		if (allocateNonPrimitiveFieldsAtOffHeapConfigType != null) {
			switch (allocateNonPrimitiveFieldsAtOffHeapConfigType) {
				case NONE:
					break;
				case ONLY_CONFIGURED_NON_PRIMITIVE_FIELDS:
					nonPrimitiveFieldInitializers = 
						OffHeapUtil.findNonPrimitiveFieldInitializersForAllNonPrimitiveFields(elementType);
					break;
				case ALL_NON_PRIMITIVE_FIELDS:
					nonPrimitiveFieldInitializers = 
						OffHeapUtil.findNonPrimitiveFieldInitializersForOnlyConfiguredNonPrimitiveFields(elementType);
					break;
			}
		}	
	}
	
	@Override
	public Class<T> getElementType() {
		return elementType;
	}
	
	public DirectMemoryService getDirectMemoryService() {
		return directMemoryService;
	}
	
	protected boolean isIn(long address) {
		return address >= allocationStartAddress && address <= allocationEndAddress;
	}
	
	protected T processObject(T obj) {
		if (nonPrimitiveFieldInitializers != null && !nonPrimitiveFieldInitializers.isEmpty()) {
			Set<Class<?>> classesInProcess = classesInProcessStore.get();
			if (classesInProcess == null) {
				classesInProcessStore.set(classesInProcess = new HashSet<Class<?>>());
			}
			try {
				classesInProcess.add(elementType);
				for (NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig> fieldInitializer : nonPrimitiveFieldInitializers) {
					if (!classesInProcess.contains(fieldInitializer.getFieldType())) {
						fieldInitializer.initializeField(obj);
					}	
				}
			}
			finally {
				classesInProcess.remove(elementType);
				if (classesInProcess.isEmpty()) {
					classesInProcessStore.remove();
				}
			}
		}
		return obj;
	}
	
	protected long processObject(long objAddress) {
		if (nonPrimitiveFieldInitializers != null && !nonPrimitiveFieldInitializers.isEmpty()) {
			Set<Class<?>> classesInProcess = classesInProcessStore.get();
			if (classesInProcess == null) {
				classesInProcessStore.set(classesInProcess = new HashSet<Class<?>>());
			}
			try {
				classesInProcess.add(elementType);
				for (NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig> fieldInitializer : nonPrimitiveFieldInitializers) {
					if (!classesInProcess.contains(fieldInitializer.getFieldType())) {
						fieldInitializer.initializeField(objAddress);
					}	
				}
			}
			finally {
				classesInProcess.remove(elementType);
				if (classesInProcess.isEmpty()) {
					classesInProcessStore.remove();
				}
			}
		}
		return objAddress;
	}
	
}
