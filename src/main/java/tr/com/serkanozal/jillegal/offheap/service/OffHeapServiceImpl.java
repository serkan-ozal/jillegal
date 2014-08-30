/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.service;

import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javassist.bytecode.Opcode;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import tr.com.serkanozal.jillegal.Jillegal;
import tr.com.serkanozal.jillegal.instrument.Instrumenter;
import tr.com.serkanozal.jillegal.instrument.domain.model.GeneratedClass;
import tr.com.serkanozal.jillegal.instrument.service.InstrumentService;
import tr.com.serkanozal.jillegal.instrument.service.InstrumentServiceFactory;
import tr.com.serkanozal.jillegal.offheap.domain.model.instance.ArrayInstanceRequest;
import tr.com.serkanozal.jillegal.offheap.domain.model.instance.InstanceRequest;
import tr.com.serkanozal.jillegal.offheap.domain.model.instance.ObjectInstanceRequest;
import tr.com.serkanozal.jillegal.offheap.domain.model.instance.StringInstanceRequest;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.pool.ArrayOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.factory.DefaultOffHeapPoolFactory;
import tr.com.serkanozal.jillegal.offheap.pool.factory.OffHeapPoolFactory;
import tr.com.serkanozal.jillegal.offheap.pool.impl.DefaultStringOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.ExtendableStringOffHeapPool;
import tr.com.serkanozal.jillegal.util.JvmUtil;
import tr.com.serkanozal.jillegal.util.ReflectionUtil;

public class OffHeapServiceImpl implements OffHeapService {

	protected final Logger logger = Logger.getLogger(getClass());
	
	protected DirectMemoryService directMemoryService = 
			DirectMemoryServiceFactory.getDirectMemoryService();
	protected OffHeapPoolFactory defaultOffHeapPoolFactory = 
			new DefaultOffHeapPoolFactory();
	protected Map<Class<? extends OffHeapPoolCreateParameter<?>>, OffHeapPoolFactory> offHeapPoolFactoryMap = 
			new ConcurrentHashMap<Class<? extends OffHeapPoolCreateParameter<?>>, OffHeapPoolFactory>();
	protected Set<Class<?>> offHeapableClasses = 
			Collections.synchronizedSet(new HashSet<Class<?>>());
	@SuppressWarnings("rawtypes")
	protected Map<Class<?>, ObjectOffHeapPool> objectOffHeapPoolMap = 
			new ConcurrentHashMap<Class<?>, ObjectOffHeapPool>();
	@SuppressWarnings("rawtypes")
	protected Set<ArrayOffHeapPool> arrayOffHeapPoolSet = 
			new HashSet<ArrayOffHeapPool>();
	protected ExtendableStringOffHeapPool extendableStringOffHeapPool;
					
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
			instrumentNonPrimitiveFieldAssignments(elementType);
			//implementNonPrimitiveFieldSetters(elementType);
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
	protected <T> void instrumentNonPrimitiveFieldAssignments(Class<T> elementType) {
		try {
			Jillegal.init();

			final InstrumentService instrumenterService = InstrumentServiceFactory.getInstrumentService();
			
			final String ownerClassDesc = elementType.getName().replace(".", "/");
			final Set<String> ownerClassDescSet = new HashSet<String>();
			
			ownerClassDescSet.add(ownerClassDesc);
			for (	Class<T> parentClass = (Class<T>) elementType.getSuperclass();
					parentClass != null && !parentClass.equals(Object.class);
					parentClass = (Class<T>) parentClass.getSuperclass()) {
				ownerClassDescSet.add(parentClass.getName().replace(".", "/"));
			}
			
			ClassReader cr = new ClassReader(elementType.getName());
			ClassWriter cw = new ClassWriter(ClassReader.SKIP_DEBUG) {
				@Override
				public MethodVisitor visitMethod(final int access, final String name,
						final String desc, final String signature, final String[] exceptions) {
					MethodVisitor mv =  super.visitMethod(access, name, desc, signature, exceptions);
					return
						new MethodAdapter(mv) {
						public void visitFieldInsn(final int opcode, final String owner,
								final String name, final String desc) {
							if (	opcode == Opcodes.PUTFIELD && 
									ownerClassDescSet.contains(owner) && 
									(desc.startsWith("L") && desc.endsWith(";"))) {
								int fieldOffset = 
										(int) JvmUtil.getUnsafe().
									objectFieldOffset(ReflectionUtil.getField(elementType, name));
								
								// Current operand stack snapshot: ..., <this>, <value_to_set>
								super.visitInsn(Opcode.SWAP); 
								
								// Current operand stack snapshot:
								//		..., <value_to_set>, <this>
								
								super.visitInsn(Opcodes.POP);
								
								// Current operand stack snapshot: 
								// 		..., <value_to_set>
								
								super.visitMethodInsn( // Call "getProperties" method of "java.lang.System" class
										Opcodes.INVOKESTATIC, 
										"java/lang/System",
							            "getProperties", 
							            "()Ljava/util/Properties;");
								
								super.visitLdcInsn(
										DirectMemoryServiceFactory.
											DIRECT_MEMORY_SERVICE_$_SET_OBJECT_FIELD_ACCESSOR); 
								
								super.visitMethodInsn( // Call "get" method of "java/util/Properties" instance
										Opcodes.INVOKEVIRTUAL, 
										"java/util/Properties",
							            "get", 
							            "(Ljava/lang/Object;)Ljava/lang/Object;");

								// Current operand stack snapshot: 
								// 		..., <value_to_set>, <DirectMemoryService_setObjectField>
								
								super.visitTypeInsn(
										Opcode.CHECKCAST, 
										PropertyChangeSupport.class.getName().replace(".", "/"));
								
								// Current operand stack snapshot: 
								// 		..., <value_to_set>, <DirectMemoryService_setObjectField>
								
								super.visitInsn(Opcode.SWAP);
								
								// Current operand stack snapshot: 
								// 		..., <DirectMemoryService_setObjectField>, <value_to_set>
								
								super.visitInsn(Opcode.ACONST_NULL);
								
								// Current operand stack snapshot: 
								// 		..., <DirectMemoryService_setObjectField>, <value_to_set>, <null>

								super.visitInsn(Opcode.SWAP);
								
								// Current operand stack snapshot: 
								// 		..., <DirectMemoryService_setObjectField>, <null>, <value_to_set> 
								
								super.visitLdcInsn(fieldOffset); // offset of field
								
								// Current operand stack snapshot:
								// 		..., <DirectMemoryService_setObjectField>, <null>, <value_to_set>, <field_offset>
								
								super.visitInsn(Opcode.SWAP);
								
								// Current operand stack snapshot:
								// 		..., <DirectMemoryService_setObjectField>, <null>, <field_offset>, <value_to_set>
								
								super.visitVarInsn(Opcodes.ALOAD, 0); // this
								
								// Current operand stack snapshot:
						 		// 		..., <DirectMemoryService_setObjectField>, <null>, <field_offset>, <value_to_set>, <this>
								
								super.visitInsn(Opcode.SWAP);
								
								// Current operand stack snapshot:
						 		// 		..., <DirectMemoryService_setObjectField>, <null>, <field_offset>, <this>, <value_to_set>, 
								
								// Now <field_offset>, <this>, <value_to_set> are the parameters in this order 
								// to be passed for "setObjectField" method "DirectMemoryService" instance
								
								super.visitMethodInsn( // Call "fireIndexedPropertyChange" method of "java/beans/PropertyChangeSupport" instance
										Opcodes.INVOKEVIRTUAL, 
										PropertyChangeSupport.class.getName().replace(".", "/"),
							            "fireIndexedPropertyChange", 
							            "(Ljava/lang/String;ILjava/lang/Object;Ljava/lang/Object;)V");
								
								logger.debug("Instrumenting assignment to field \"" + name  + 
										"\" in class " + elementType.getName());
							}
							else {
								super.visitFieldInsn(opcode, owner, name, desc);
							}
						}
					};
				}
			};
			cr.accept(cw, ClassReader.SKIP_DEBUG);
			
			byte[] instrumentedBytecodes = cw.toByteArray();
			
			instrumenterService.redefineClass(elementType, instrumentedBytecodes);
			
			logger.info("Instrumented class " + elementType.getName() + " for non-primitive field assignment");
		}
		catch (Throwable t) {
			t.printStackTrace();
			logger.error("Error occured while instrumenting non-primitive field assignments for class " + 
							elementType.getName(), t);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstance(InstanceRequest<T> request) {
		if (request instanceof ObjectInstanceRequest) {
			ObjectInstanceRequest<T> objectInstanceRequest = (ObjectInstanceRequest<T>)request;
			return newObject(objectInstanceRequest.getObjectType());
		}
		else if (request instanceof ArrayInstanceRequest) {
			ArrayInstanceRequest<T> arrayInstanceRequest = (ArrayInstanceRequest<T>)request;
			return newArray(arrayInstanceRequest.getArrayType(), arrayInstanceRequest.getLength());
		}
		else if (request instanceof StringInstanceRequest) {
			StringInstanceRequest stringInstanceRequest = (StringInstanceRequest)request;
			return (T) newString(stringInstanceRequest.getString());
		}
		else {
			throw 
				new IllegalArgumentException(
						"Unsupported instance request type: " + request.getClass().getName());
		}
	}
	
	@Override
	public <T> long newInstanceAsAddress(InstanceRequest<T> request) {
		if (request instanceof ObjectInstanceRequest) {
			ObjectInstanceRequest<T> objectInstanceRequest = (ObjectInstanceRequest<T>)request;
			return newObjectAsAddress(objectInstanceRequest.getObjectType());
		}
		else if (request instanceof ArrayInstanceRequest) {
			ArrayInstanceRequest<T> arrayInstanceRequest = (ArrayInstanceRequest<T>)request;
			return newArrayAsAddress(arrayInstanceRequest.getArrayType(), arrayInstanceRequest.getLength());
		}
		else if (request instanceof StringInstanceRequest) {
			StringInstanceRequest stringInstanceRequest = (StringInstanceRequest)request;
			return newStringAsAddress(stringInstanceRequest.getString());
		}
		else {
			throw 
				new IllegalArgumentException(
						"Unsupported instance request type: " + request.getClass().getName());
		}
	}
	
	@Override
	public <T> boolean freeInstance(T instance) {
		if (instance.getClass().isArray()) {
			return freeArray(instance);
		}
		else if (instance.getClass().equals(String.class)) {
			return freeString((String)instance);
		}
		else {
			return freeObject(instance);
		}
	}
	
	@Override
	public boolean freeInstanceWithAddress(long address) {
		Object instance = directMemoryService.getObject(address);
		if (instance.getClass().isArray()) {
			return freeArrayWithAddress(address);
		}
		else if (instance.getClass().equals(String.class)) {
			return freeStringWithAddress(address);
		}
		else {
			return freeObjectWithAddress(address);
		}
	}
	
	@Override
	public <T> boolean isFreeInstance(T instance) {
		return isFreeInstanceWithAddress(directMemoryService.addressOf(instance));
	}
	
	@Override
	public boolean isFreeInstanceWithAddress(long address) {
		return directMemoryService.getInt(address) == 0;
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> boolean freeObject(T obj) {
		for (Map.Entry<Class<?>, ObjectOffHeapPool> objectOffHeapPoolEntry : objectOffHeapPoolMap.entrySet()) {
			if (objectOffHeapPoolEntry.getValue().free(obj)) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean freeObjectWithAddress(long address) {
		for (Map.Entry<Class<?>, ObjectOffHeapPool> objectOffHeapPoolEntry : objectOffHeapPoolMap.entrySet()) {
			if (objectOffHeapPoolEntry.getValue().freeFromAddress(address)) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public synchronized <A> A newArray(Class<A> arrayType, int length) {
		ArrayOffHeapPool arrayOffHeapPool =
				defaultOffHeapPoolFactory.
					createArrayOffHeapPool(arrayType, length);
		arrayOffHeapPoolSet.add(arrayOffHeapPool);
		return (A) arrayOffHeapPool.getArray();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public synchronized <A> long newArrayAsAddress(Class<A> arrayType, int length) {
		ArrayOffHeapPool arrayOffHeapPool =
				defaultOffHeapPoolFactory.
					createArrayOffHeapPool(arrayType, length);
		arrayOffHeapPoolSet.add(arrayOffHeapPool);
		return arrayOffHeapPool.getArrayAsAddress();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public synchronized <A> boolean freeArray(A array) {
		ArrayOffHeapPool arrayOffHeapPoolToRemove = null;
		for (ArrayOffHeapPool arrayOffHeapPool : arrayOffHeapPoolSet) {
			if (arrayOffHeapPool.isMe(array)) {
				arrayOffHeapPoolToRemove = arrayOffHeapPool;
				break;
			}
		}
		if (arrayOffHeapPoolToRemove != null) {
			arrayOffHeapPoolToRemove.free();
			arrayOffHeapPoolSet.remove(arrayOffHeapPoolToRemove);
			return true;
		}
		else {
			return false;
		}	
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean freeArrayWithAddress(long address) {
		ArrayOffHeapPool arrayOffHeapPoolToRemove = null;
		for (ArrayOffHeapPool arrayOffHeapPool : arrayOffHeapPoolSet) {
			if (arrayOffHeapPool.isMeAsAddress(address)) {
				arrayOffHeapPoolToRemove = arrayOffHeapPool;
				break;
			}
		}
		if (arrayOffHeapPoolToRemove != null) {
			arrayOffHeapPoolToRemove.free();
			arrayOffHeapPoolSet.remove(arrayOffHeapPoolToRemove);
			return true;
		}
		else {
			return false;
		}	
	}
	
	@Override
	public String newString(String str) {
		if (extendableStringOffHeapPool == null) {
			extendableStringOffHeapPool = 
					new ExtendableStringOffHeapPool(new DefaultStringOffHeapPool());
		}
		return extendableStringOffHeapPool.get(str);
	}
	
	@Override
	public long newStringAsAddress(String str) {
		if (extendableStringOffHeapPool == null) {
			extendableStringOffHeapPool = 
					new ExtendableStringOffHeapPool(new DefaultStringOffHeapPool());
		}
		return extendableStringOffHeapPool.getAsAddress(str);
	}
	
	@Override
	public boolean freeString(String str) {
		return extendableStringOffHeapPool.free(str);
	}
	
	@Override
	public boolean freeStringWithAddress(long address) {
		return extendableStringOffHeapPool.freeFromAddress(address);
	}
	
}
