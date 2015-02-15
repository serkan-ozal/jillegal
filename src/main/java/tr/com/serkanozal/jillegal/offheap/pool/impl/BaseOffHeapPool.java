/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

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
import tr.com.serkanozal.jillegal.util.JvmUtil;

public abstract class BaseOffHeapPool<T, P extends OffHeapPoolCreateParameter<T>> implements OffHeapPool<T, P> {

	protected final Logger logger = Logger.getLogger(getClass());
	
	protected Class<T> elementType;
	protected DirectMemoryService directMemoryService;
	protected NonPrimitiveFieldAllocationConfigType allocateNonPrimitiveFieldsAtOffHeapConfigType;
	protected Set<NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig>> nonPrimitiveFieldInitializers;
	protected OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	protected OffHeapConfigService offHeapConfigService = ConfigManager.getOffHeapConfigService();
	protected long allocationStartAddress;
	protected long allocationEndAddress;
	protected long allocationSize;
	protected volatile boolean available = false;
	protected volatile boolean inProgress = false;
	
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
	
	protected void init() {
		
	}
	
	protected void makeAvaiable() {
		available = true;
	}
	
	protected void makeUnavaiable() {
		available = false;
	}
	
	protected void checkAvailability() {
		if (!available) {
			throw new IllegalStateException(getClass() + " is not available !");
		}
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
						OffHeapUtil.findNonPrimitiveFieldInitializersForOnlyConfiguredNonPrimitiveFields(elementType);
					break;
				case ALL_NON_PRIMITIVE_FIELDS:
					nonPrimitiveFieldInitializers = 
						OffHeapUtil.findNonPrimitiveFieldInitializersForAllNonPrimitiveFields(elementType);
					break;
			}
		}
	}
	
	protected byte getBit(byte value, byte bit) {
		return (byte) ((value & (1 << bit)) == 0 ? 0 : 1);
	}
	
	protected byte setBit(byte value, byte bit) {
		return (byte) (value | (1 << bit));
	}
	
	protected byte unsetBit(byte value, byte bit) {
		return (byte) (value & (~(1 << bit)));
	}
	
	@Override
	public Class<T> getElementType() {
		return elementType;
	}
	
	@Override
	public boolean isAvailable() {
		return available;
	}
	
	public DirectMemoryService getDirectMemoryService() {
		return directMemoryService;
	}
	
	protected boolean isIn(long address) {
		boolean result = address >= allocationStartAddress && address <= allocationEndAddress;
//		if (!result) {
//			System.out.println(elementType + " typed object with address " + 
//					JvmUtil.toHexAddress(address) + " is not in in between " + 
//					JvmUtil.toHexAddress(allocationStartAddress) + " and " + 
//					JvmUtil.toHexAddress(allocationEndAddress));
//		}
		return result;
	}
	
	protected synchronized T processObject(T obj) {
		if (!inProgress && nonPrimitiveFieldInitializers != null && !nonPrimitiveFieldInitializers.isEmpty()) {
			try {
				inProgress = true;
				for (NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig> fieldInitializer : nonPrimitiveFieldInitializers) {
					fieldInitializer.initializeField(obj);	
				}
			}
			finally {
				inProgress = false;
			}
		}
		return obj;
	}
	
	protected synchronized long processObject(long objAddress) {
		if (!inProgress && nonPrimitiveFieldInitializers != null && !nonPrimitiveFieldInitializers.isEmpty()) {
			try {
				inProgress = true;
				for (NonPrimitiveFieldInitializer<? extends OffHeapFieldConfig> fieldInitializer : nonPrimitiveFieldInitializers) {
					fieldInitializer.initializeField(objAddress);
				}
			}
			finally {
				inProgress = false;
			}
		}
		return objAddress;
	}
	
}
