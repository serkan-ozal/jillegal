/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jillegal.Jillegal;
import tr.com.serkanozal.jillegal.instrument.Instrumenter;
import tr.com.serkanozal.jillegal.instrument.domain.model.GeneratedClass;
import tr.com.serkanozal.jillegal.instrument.service.InstrumentService;
import tr.com.serkanozal.jillegal.instrument.service.InstrumentServiceFactory;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.factory.DefaultOffHeapPoolFactory;
import tr.com.serkanozal.jillegal.offheap.pool.factory.OffHeapPoolFactory;
import tr.com.serkanozal.jillegal.util.ReflectionUtil;

public class OffHeapServiceImpl implements OffHeapService {

	protected final Logger logger = Logger.getLogger(getClass());
	
	protected OffHeapPoolFactory defaultOffHeapPoolFactory = new DefaultOffHeapPoolFactory();
	protected Map<Class<? extends OffHeapPoolCreateParameter<?>>, OffHeapPoolFactory> offHeapPoolFactoryMap = 
			new ConcurrentHashMap<Class<? extends OffHeapPoolCreateParameter<?>>, OffHeapPoolFactory>();
	protected Set<Class<?>> offHeapableClasses = Collections.synchronizedSet(new HashSet<Class<?>>());
	@SuppressWarnings("rawtypes")
	protected Map<Class<?>, ObjectOffHeapPool> objectOffHeapPoolMap = new ConcurrentHashMap<Class<?>, ObjectOffHeapPool>();
	
	public OffHeapServiceImpl() {
		init();
	}
	
	protected void init() {
		
	}

	protected <P extends OffHeapPoolCreateParameter<?>> OffHeapPoolFactory findOffHeapPoolFactory(Class<P> clazz) {
		OffHeapPoolFactory offHeapPoolFactory = offHeapPoolFactoryMap.get(clazz);
		if (offHeapPoolFactory != null) {
			return offHeapPoolFactory;
		}
		else {
			return defaultOffHeapPoolFactory;
		}
	}
	
	@Override
	public OffHeapPoolFactory getDefaultOffHeapPoolFactory() {
		return defaultOffHeapPoolFactory;
	}

	@Override
	public void setOffHeapPoolFactory(OffHeapPoolFactory offHeapPoolFactory) {
		defaultOffHeapPoolFactory = offHeapPoolFactory;
	}

	@Override
	public <P extends OffHeapPoolCreateParameter<?>> OffHeapPoolFactory getOffHeapPoolFactory(Class<P> clazz) {
		return offHeapPoolFactoryMap.get(clazz);
	}

	@Override
	public <P extends OffHeapPoolCreateParameter<?>> void setOffHeapPoolFactory(OffHeapPoolFactory offHeapPoolFactory, Class<P> clazz) {
		offHeapPoolFactoryMap.put(clazz, offHeapPoolFactory);
	}
	
	@Override
	public <T, O extends OffHeapPool<T, ?>> O createOffHeapPool(OffHeapPoolCreateParameter<T> parameter) {
		OffHeapPoolFactory offHeapPoolFactory = findOffHeapPoolFactory(parameter.getClass());
		if (offHeapPoolFactory != null) {
			if (parameter.isMakeOffHeapableAsAuto()) {
				makeOffHeapable(parameter.getElementType());
			}
			return offHeapPoolFactory.createOffHeapPool(parameter);
		}
		else {
			logger.warn("OffHeapPool couldn't be found for class " + parameter.getClass().getName());
			return null;
		}
	}
	
	@Override
	public synchronized <T> void makeOffHeapable(Class<T> elementType) {
		if (!offHeapableClasses.contains(elementType)) {
			implementNonPrimitiveFieldSetters(elementType);
			offHeapableClasses.add(elementType);
		}
	}
	
	protected <T> void implementNonPrimitiveFieldSetters(Class<T> elementType) {
		try {
			Jillegal.init();
			
			InstrumentService instrumenterService = InstrumentServiceFactory.getInstrumentService();
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

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T> T newObject(Class<T> objectType) {
		ObjectOffHeapPool<T, ?> objectOffHeapPool = objectOffHeapPoolMap.get(objectType);
		if (objectOffHeapPool == null) {
			objectOffHeapPool = 
					defaultOffHeapPoolFactory.createObjectOffHeapPool(
							objectType, OffHeapConstants.DEFAULT_OBJECT_COUNT);
			objectOffHeapPoolMap.put(objectType, objectOffHeapPool);
		}
		return objectOffHeapPool.get();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T> long newObjectAsAddress(Class<T> objectType) {
		ObjectOffHeapPool<T, ?> objectOffHeapPool = objectOffHeapPoolMap.get(objectType);
		if (objectOffHeapPool == null) {
			objectOffHeapPool = 
					defaultOffHeapPoolFactory.createObjectOffHeapPool(
							objectType, OffHeapConstants.DEFAULT_OBJECT_COUNT);
			objectOffHeapPoolMap.put(objectType, objectOffHeapPool);
		}
		return objectOffHeapPool.getAsAddress();
	}

	@Override
	public <T> void freeObject(T obj) {
		throw new UnsupportedOperationException("\"void freeObject(T obj)\" is not supported right now !");
	}
	
	@Override
	public void freeObjectWithAddress(long address) {
		throw new UnsupportedOperationException("\"void freeObjectWithAddress(long address)\" is not supported right now !");
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <A> A newArray(Class<A> arrayType, int length) {
		return
			(A) defaultOffHeapPoolFactory.createArrayOffHeapPool(
					arrayType, OffHeapConstants.DEFAULT_OBJECT_COUNT).
						getArray();
	}
	
	@Override
	public synchronized <A> long newArrayAsAddress(Class<A> arrayType, int length) {
		return
			defaultOffHeapPoolFactory.createArrayOffHeapPool(
					arrayType, OffHeapConstants.DEFAULT_OBJECT_COUNT).
						getArrayAsAddress();
	}

	@Override
	public <A> void freeArray(A array) {
		throw new UnsupportedOperationException("\"void freeArray(T array)\" is not supported right now !");
	}
	
	@Override
	public void freeArrayWithAddress(long address) {
		throw new UnsupportedOperationException("\"void freeArrayWithAddress(long address)\" is not supported right now !");
	}
	
}
