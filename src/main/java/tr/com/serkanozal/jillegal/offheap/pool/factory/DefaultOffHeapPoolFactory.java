/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.pool.factory;

import java.lang.reflect.Modifier;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ArrayOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.BaseOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ExtendableArrayOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ExtendableObjectOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ExtendableStringOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolType;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.StringOffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.pool.ArrayOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.ComplexTypeArrayOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.DefaultForkableObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.EagerReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.ExtendableArrayOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.ExtendableObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.ExtendableStringOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.LazyReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.PrimitiveTypeArrayOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.DefaultStringOffHeapPool;

public class DefaultOffHeapPoolFactory implements OffHeapPoolFactory {

	private DirectMemoryService directMemoryService = DirectMemoryServiceFactory.getDirectMemoryService();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T, O extends OffHeapPool> O  createOffHeapPool(OffHeapPoolCreateParameter<T> parameter) {
		Class<?> elementType = parameter.getElementType();
		if (elementType.equals(String[].class)) {
			throw new IllegalArgumentException("\"String[]\" types are not supported yet !");
		}
		
		assignOffHeapMemoryServiceIfNeeded(parameter);
		
		OffHeapPoolType offHeapPoolType = parameter.getOffHeapPoolType();
		switch (offHeapPoolType) {
			case OBJECT_POOL:
				ObjectOffHeapPoolCreateParameter<T> seqObjPoolParameter = (ObjectOffHeapPoolCreateParameter<T>)parameter; 
				switch (seqObjPoolParameter.getReferenceType()) {
					case LAZY_REFERENCED:
						return (O) new LazyReferencedObjectOffHeapPool<T>((ObjectOffHeapPoolCreateParameter<T>)parameter);
					case EAGER_REFERENCED:
						return (O) new EagerReferencedObjectOffHeapPool<T>((ObjectOffHeapPoolCreateParameter<T>)parameter);
					default:
						return (O) new LazyReferencedObjectOffHeapPool<T>((ObjectOffHeapPoolCreateParameter<T>)parameter);
				}
			case ARRAY_POOL:
				ArrayOffHeapPoolCreateParameter<T> arrayPoolParameter = (ArrayOffHeapPoolCreateParameter<T>)parameter; 
				if (arrayPoolParameter.isUsePrimitiveTypes()) {
					return (O) new PrimitiveTypeArrayOffHeapPool<T, T[]>(arrayPoolParameter);
				}
				else {
					return (O) new ComplexTypeArrayOffHeapPool<T, T[]>(arrayPoolParameter);
				}
			case STRING_POOL:
				return (O) new DefaultStringOffHeapPool((StringOffHeapPoolCreateParameter)parameter);
			case EXTENDABLE_OBJECT_POOL:
				return (O) new ExtendableObjectOffHeapPool<T>((ExtendableObjectOffHeapPoolCreateParameter<T>)parameter);
			case EXTENDABLE_ARRAY_POOL:
				return (O) new ExtendableArrayOffHeapPool<T, T[]>((ExtendableArrayOffHeapPoolCreateParameter<T, T[]>)parameter);
			case EXTENDABLE_STRING_POOL:
				return (O) new ExtendableStringOffHeapPool((ExtendableStringOffHeapPoolCreateParameter)parameter);	
			default:
				throw new IllegalArgumentException("Unsupported off heap pool type: " + offHeapPoolType);
		}
	}
	
	protected <T> void assignOffHeapMemoryServiceIfNeeded(OffHeapPoolCreateParameter<T> parameter) {
		if (parameter instanceof BaseOffHeapPoolCreateParameter) {
			BaseOffHeapPoolCreateParameter<T> baseParameter = (BaseOffHeapPoolCreateParameter<T>)parameter;
			if (baseParameter.getDirectMemoryService() == null) {
				baseParameter.setDirectMemoryService(directMemoryService);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T, O extends ObjectOffHeapPool> O createObjectOffHeapPool(
			Class<T> objectType, int objectCount) {
		return 
			(O) new ExtendableObjectOffHeapPool<T>(
					objectType, 
					new DefaultForkableObjectOffHeapPool<T>(objectType, objectCount), 
					directMemoryService);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T, A, O extends ArrayOffHeapPool> O createArrayOffHeapPool(
			Class<A> arrayType, int arrayLength) {
		if (!arrayType.isArray()) {
			throw new IllegalArgumentException(arrayType.getClass().getName() + " is not array type");
		}
		Class elementType = arrayType.getComponentType();
		if (elementType.isPrimitive()) {
			return 
				(O) new PrimitiveTypeArrayOffHeapPool<T, A>(elementType, arrayLength, directMemoryService);
		}
		else {
			boolean initializeElements = !elementType.isInterface() && !Modifier.isAbstract(elementType.getModifiers());
			return
				(O) new ComplexTypeArrayOffHeapPool<T, A>(elementType, arrayLength, initializeElements, directMemoryService);
		}
	}

}
