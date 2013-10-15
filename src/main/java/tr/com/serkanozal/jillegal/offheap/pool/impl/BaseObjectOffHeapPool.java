/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import tr.com.serkanozal.jcommon.util.ReflectionUtil;
import tr.com.serkanozal.jillegal.Jillegal;
import tr.com.serkanozal.jillegal.instrument.Instrumenter;
import tr.com.serkanozal.jillegal.instrument.domain.model.GeneratedClass;
import tr.com.serkanozal.jillegal.instrument.service.InstrumenterService;
import tr.com.serkanozal.jillegal.instrument.service.InstrumenterServiceFactory;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.util.JvmUtil;

public abstract class BaseObjectOffHeapPool<T, P extends OffHeapPoolCreateParameter<T>> extends BaseOffHeapPool<T, P> {

	protected int objectCount;
	protected long objectSize;
	protected long currentAddress;
	protected long allocatedAddress;
	protected T sampleObject;
	protected long offHeapSampleObjectAddress;
	protected long classPointerAddress;
	protected long classPointerOffset;
	protected long classPointerSize;
	protected long addressLimit;
	protected boolean autoImplementNonPrimitiveFieldSetters;
	protected JvmAwareClassPointerUpdater jvmAwareClassPointerUpdater;
	
	public BaseObjectOffHeapPool(Class<T> elementType, boolean autoImplementNonPrimitiveFieldSetters) {
		super(elementType);
		this.autoImplementNonPrimitiveFieldSetters = autoImplementNonPrimitiveFieldSetters;
		if (autoImplementNonPrimitiveFieldSetters) {
			implementNonPrimitiveFieldSetters();
		}
	}
	
	public BaseObjectOffHeapPool(Class<T> elementType, boolean autoImplementNonPrimitiveFieldSetters, 
			DirectMemoryService directMemoryService) {
		super(elementType, directMemoryService);
		this.autoImplementNonPrimitiveFieldSetters = autoImplementNonPrimitiveFieldSetters;
		if (autoImplementNonPrimitiveFieldSetters) {
			implementNonPrimitiveFieldSetters();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void init(Class<T> elementType, int objectCount, DirectMemoryService directMemoryService) {
		if (elementType.isAnnotation()) {
			throw new IllegalArgumentException("Annotation class " + "(" + elementType.getName() + ")" + 
											   " is not supported !");
		}
		if (elementType.isInterface()) {
			throw new IllegalArgumentException("Interface class " + "(" + elementType.getName() + ")" + 
											   " is not supported !");
		}
		if (elementType.isAnonymousClass()) {
			throw new IllegalArgumentException("Anonymous class " + "(" + elementType.getName() + ")" + 
											   " is not supported !");
		}
		this.elementType = elementType;
		this.objectCount = objectCount;
		this.directMemoryService = directMemoryService;
		this.objectSize = directMemoryService.sizeOf(elementType);
		boolean sampleObjectCreated = false;
		try {
			Constructor<T> defaultConstructor = elementType.getConstructor();
			if (defaultConstructor != null) {
				defaultConstructor.setAccessible(true);
				this.sampleObject = defaultConstructor.newInstance();
			}
		} 
		catch (Throwable t) {
			logger.error("Unable to create a sample object for class " + elementType.getName(), t);
		} 
		if (sampleObjectCreated == false) {
			this.sampleObject = (T) directMemoryService.allocateInstance(elementType);
		}
		long address = directMemoryService.addressOf(sampleObject);
		this.offHeapSampleObjectAddress = directMemoryService.allocateMemory(objectSize);
		directMemoryService.copyMemory(address, offHeapSampleObjectAddress, objectSize);
		this.classPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
		this.classPointerOffset = JvmUtil.getClassDefPointerOffsetInObject();
		this.classPointerSize = JvmUtil.getReferenceSize();
		switch (JvmUtil.getAddressSize()) {
	        case JvmUtil.SIZE_32_BIT:
	        	jvmAwareClassPointerUpdater = new Address32BitJvmClassPointerUpdater();
	            break;
	        case JvmUtil.SIZE_64_BIT:
	        	int referenceSize = JvmUtil.getReferenceSize();
	        	switch (referenceSize) {
	             	case JvmUtil.ADDRESSING_4_BYTE:   
	             		jvmAwareClassPointerUpdater = new Address64BitWithCompressedOopsJvmClassPointerUpdater();
	             		break;
	             	case JvmUtil.ADDRESSING_8_BYTE:
	             		jvmAwareClassPointerUpdater = new Address64BitWithoutCompressedOopsJvmClassPointerUpdater();
	             		break;
	             	default:    
	                    throw new AssertionError("Unsupported reference size: " + referenceSize);
	        	}
	        	break;    
	        default:
	            throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());
		} 
	}
	
	protected boolean checkAndRefreshIfClassPointerOfObjectUpdated() {
		long currentClassPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
		if (currentClassPointerAddress != classPointerAddress) {
			classPointerAddress = currentClassPointerAddress;
			return true;
		}
		else {
			return false;
		}
	}
	
	protected interface JvmAwareClassPointerUpdater {
		
		long updateClassPointerOfObject(long address);
	
	}
	
	protected class Address32BitJvmClassPointerUpdater implements JvmAwareClassPointerUpdater {

		@Override
		public long updateClassPointerOfObject(long address) {
			classPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
			directMemoryService.putAsIntAddress(address + classPointerOffset, classPointerAddress);
			return address;
		}
		
	}
	
	protected class Address64BitWithoutCompressedOopsJvmClassPointerUpdater implements JvmAwareClassPointerUpdater {

		@Override
		public long updateClassPointerOfObject(long address) {
			classPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
			directMemoryService.putAsIntAddress(address + classPointerOffset, classPointerAddress);
			return address;
		}
		
	}
	
	protected class Address64BitWithCompressedOopsJvmClassPointerUpdater implements JvmAwareClassPointerUpdater {

		@Override
		public long updateClassPointerOfObject(long address) {
			classPointerAddress = JvmUtil.jvmAddressOfClass(sampleObject);
			directMemoryService.putLong(address + classPointerOffset, classPointerAddress);
			return address;
		}
		
	}
	
	/**
	 * Get and copy class pointer to current object's class pointer field. 
	 * Address of class could be changed by GC at "Compact" phase.
	 * 
	 * @param address
	 */
	protected long updateClassPointerOfObject(long address) {
		return jvmAwareClassPointerUpdater.updateClassPointerOfObject(address);
	}
	
	protected void implementNonPrimitiveFieldSetters() {
		try {
			Jillegal.init();
			
			InstrumenterService instrumenterService = InstrumenterServiceFactory.getInstrumenterService();
	        Instrumenter<T> instrumenter = instrumenterService.getInstrumenter(elementType).
	        									addAdditionalClass(DirectMemoryServiceFactory.class).
	        									addAdditionalClass(DirectMemoryService.class);

			List<Field> fields = ReflectionUtil.getAllFields(elementType);
			if (fields != null) {
				for (Field field : fields) {
					if (ReflectionUtil.isNonPrimitiveType(field.getType())) {
						String fieldName = field.getName();
						String setterMethodName = "set" + Character.toUpperCase(fieldName.charAt(0)) + 
														  fieldName.substring(1);
						Method setterMethod = 
								ReflectionUtil.getMethod(elementType, setterMethodName, 
														 new Class<?>[] {field.getType()});
						if (setterMethod != null) {
							String setterImplCode = "DirectMemoryServiceFactory.getDirectMemoryService().setObjectField" + 
													"("  + 
														"this" + ", " + "\"" + field.getName() + "\"" + ", " + "$1" + 
													");";
							try {
								instrumenter = 
									instrumenter.updateMethod(
										setterMethodName, 
										setterImplCode, 
						        		setterMethod.getParameterTypes());
							}
							catch (Throwable t) {
								logger.error("Unable to instrument method " + setterMethod.toString() + 
											 " with implementation code " + "\"" + setterImplCode + "\"", t);
							}
						}
					}
				}
			}
			
			GeneratedClass<T> instrumentedClass = instrumenter.build();
			instrumenterService.redefineClass(instrumentedClass);
		}
		catch (Throwable t) {
			logger.error("Error occured while implementing non-primitive field setters for class " + 
							elementType.getName(), t);
		}
	}
	
}
