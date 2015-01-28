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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.Opcode;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import sun.misc.Unsafe;
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
import tr.com.serkanozal.jillegal.offheap.pool.ContentAwareOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.factory.DefaultOffHeapPoolFactory;
import tr.com.serkanozal.jillegal.offheap.pool.factory.OffHeapPoolFactory;
import tr.com.serkanozal.jillegal.offheap.pool.impl.DefaultStringOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.impl.ExtendableStringOffHeapPool;
import tr.com.serkanozal.jillegal.util.JvmUtil;
import tr.com.serkanozal.jillegal.util.ReflectionUtil;

@SuppressWarnings( { "restriction" } )
public class OffHeapServiceImpl implements OffHeapService {

	protected static final Logger logger = Logger.getLogger(OffHeapServiceImpl.class);
	
	protected static final Unsafe UNSAFE = JvmUtil.getUnsafe();
	
	protected static final long BOOLEAN_VALUE_FIELD_OFFSET;
	protected static final long BYTE_VALUE_FIELD_OFFSET;
	protected static final long CHARACTER_VALUE_FIELD_OFFSET;
	protected static final long SHORT_VALUE_FIELD_OFFSET;
	protected static final long INTEGER_VALUE_FIELD_OFFSET;
	protected static final long FLOAT_VALUE_FIELD_OFFSET;
	protected static final long LONG_VALUE_FIELD_OFFSET;
	protected static final long DOUBLE_VALUE_FIELD_OFFSET;
	
	static {
		try {
			BOOLEAN_VALUE_FIELD_OFFSET = UNSAFE.objectFieldOffset(JvmUtil.getField(Boolean.class, "value"));
			BYTE_VALUE_FIELD_OFFSET = UNSAFE.objectFieldOffset(JvmUtil.getField(Byte.class, "value"));
			CHARACTER_VALUE_FIELD_OFFSET = UNSAFE.objectFieldOffset(JvmUtil.getField(Character.class, "value"));
			SHORT_VALUE_FIELD_OFFSET = UNSAFE.objectFieldOffset(JvmUtil.getField(Short.class, "value"));
			INTEGER_VALUE_FIELD_OFFSET = UNSAFE.objectFieldOffset(JvmUtil.getField(Integer.class, "value"));
			FLOAT_VALUE_FIELD_OFFSET = UNSAFE.objectFieldOffset(JvmUtil.getField(Float.class, "value"));
			LONG_VALUE_FIELD_OFFSET = UNSAFE.objectFieldOffset(JvmUtil.getField(Long.class, "value"));
			DOUBLE_VALUE_FIELD_OFFSET = UNSAFE.objectFieldOffset(JvmUtil.getField(Double.class, "value"));
		} 
		catch (Throwable t) {
			logger.error("Unable to initialize " + OffHeapServiceImpl.class, t);
			throw new IllegalStateException("Unable to initialize " + OffHeapServiceImpl.class, t);
		}
	}
	
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
	protected List<ObjectOffHeapPool> objectOffHeapPoolList = 
			new ArrayList<ObjectOffHeapPool>();
	@SuppressWarnings("rawtypes")
	protected Set<ArrayOffHeapPool> arrayOffHeapPoolSet = 
			new HashSet<ArrayOffHeapPool>();
	protected ExtendableStringOffHeapPool stringOffHeapPool;
	protected boolean enable = false;
					
	public OffHeapServiceImpl() {
		init();
	}
	
	protected void init() {
		findEnable();
	}
	
	protected void findEnable() {
		boolean jvmOk = false;
		if (JvmUtil.isHotspotJvm()) {
			jvmOk = JvmUtil.isJava_8();
		} 
		else if (JvmUtil.isJRockitJvm() || JvmUtil.isIBMJvm()) {
			jvmOk = true;
		}
		boolean compressedOopsOk = !JvmUtil.isCompressedRef();
		enable = jvmOk && compressedOopsOk;
	}
	
	protected void checkEnable() {
		if (!enable) {
			throw new IllegalStateException(
					"OffHeap module is noly available on Hotspot JDK/JRE 8, JRockit JVM and IBM JVM. " +
					"In addition \"compressedRef\" must be disabled on 64 bit JVM. " + 
					"You can use \"-XX:-UseCompressedOops\" as VM argument to disable compressed references on 64 bit JVM.");
		}
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
	public boolean isEnable() {
		return enable;
	}
	
	@Override
	public DirectMemoryService getDirectMemoryService() {
		return directMemoryService;
	}
	
	@Override
	public void setDirectMemoryService(DirectMemoryService directMemoryService) {
		this.directMemoryService = directMemoryService;
	}
	
	@Override
	public OffHeapPoolFactory getDefaultOffHeapPoolFactory() {
		checkEnable();
		
		return defaultOffHeapPoolFactory;
	}

	@Override
	public void setOffHeapPoolFactory(OffHeapPoolFactory offHeapPoolFactory) {
		checkEnable();
		
		defaultOffHeapPoolFactory = offHeapPoolFactory;
	}

	@Override
	public <P extends OffHeapPoolCreateParameter<?>> OffHeapPoolFactory getOffHeapPoolFactory(Class<P> clazz) {
		checkEnable();
		
		return offHeapPoolFactoryMap.get(clazz);
	}

	@Override
	public <P extends OffHeapPoolCreateParameter<?>> void setOffHeapPoolFactory(Class<P> clazz, 
			OffHeapPoolFactory offHeapPoolFactory) {
		checkEnable();
		
		offHeapPoolFactoryMap.put(clazz, offHeapPoolFactory);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T, P extends OffHeapPoolCreateParameter<T>> ObjectOffHeapPool<T, P> getObjectOffHeapPool(Class<T> clazz) {
		checkEnable();
		
		return objectOffHeapPoolMap.get(clazz);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T, P extends OffHeapPoolCreateParameter<T>> void setObjectOffHeapPool(Class<T> clazz, 
			ObjectOffHeapPool<T, P> objectOffHeapPool) {
		checkEnable();
		
		ObjectOffHeapPool<T, ?> oldObjectOffHeapPool = objectOffHeapPoolMap.put(clazz, objectOffHeapPool);
		if (oldObjectOffHeapPool != null) {
			objectOffHeapPoolList.remove(oldObjectOffHeapPool);
		}
		objectOffHeapPoolList.add(objectOffHeapPool);
	}

	@Override
	public <T, O extends OffHeapPool<T, ?>> O createOffHeapPool(OffHeapPoolCreateParameter<T> parameter) {
		checkEnable();
		
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
		checkEnable();
		
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
	protected <T> void instrumentNonPrimitiveFieldAssignments(final Class<T> elementType) {
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
			
			String className = elementType.getName();
			
			ClassPool cp = ClassPool.getDefault();
			CtClass clazz = cp.get(className);
			
			ClassReader cr = new ClassReader(clazz.toBytecode());
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
			// t.printStackTrace();
			logger.error("Error occured while instrumenting non-primitive field assignments for class " + 
							elementType.getName(), t);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstance(InstanceRequest<T> request) {
		checkEnable();
		
		if (request instanceof ObjectInstanceRequest) {
			ObjectInstanceRequest<T> objectInstanceRequest = (ObjectInstanceRequest<T>)request;
			return newObject(objectInstanceRequest.getObjectType());
		}
		else if (request instanceof ArrayInstanceRequest) {
			ArrayInstanceRequest<T> arrayInstanceRequest = (ArrayInstanceRequest<T>)request;
			return newArray(arrayInstanceRequest.getArrayType(), arrayInstanceRequest.getLength(), 
					arrayInstanceRequest.isInitializeElements());
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
		checkEnable();
		
		if (request instanceof ObjectInstanceRequest) {
			ObjectInstanceRequest<T> objectInstanceRequest = (ObjectInstanceRequest<T>)request;
			return newObjectAsAddress(objectInstanceRequest.getObjectType());
		}
		else if (request instanceof ArrayInstanceRequest) {
			ArrayInstanceRequest<T> arrayInstanceRequest = (ArrayInstanceRequest<T>)request;
			return newArrayAsAddress(arrayInstanceRequest.getArrayType(), arrayInstanceRequest.getLength(),
					arrayInstanceRequest.isInitializeElements());
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
		checkEnable();
		
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
		checkEnable();
		
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
		checkEnable();
		
		return isFreeInstanceWithAddress(directMemoryService.addressOf(instance));
	}
	
	@Override
	public boolean isFreeInstanceWithAddress(long address) {
		checkEnable();
		
		return directMemoryService.getInt(address) == 0;
	}
	
	@SuppressWarnings("unchecked")
	private <T> ObjectOffHeapPool<T, ?> getObjectOffHeapPoolSynchronized(Class<T> objectType) {
		ObjectOffHeapPool<T, ?> objectOffHeapPool = objectOffHeapPoolMap.get(objectType);
		if (objectOffHeapPool == null) {
			synchronized (objectOffHeapPoolMap) {
				objectOffHeapPool = objectOffHeapPoolMap.get(objectType);
				if (objectOffHeapPool == null) {
					objectOffHeapPool = 
							defaultOffHeapPoolFactory.createObjectOffHeapPool(
									objectType, OffHeapConstants.DEFAULT_OBJECT_COUNT);
					ObjectOffHeapPool<T, ?> oldObjectOffHeapPool = 
							objectOffHeapPoolMap.put(objectType, objectOffHeapPool);
					if (oldObjectOffHeapPool != null) {
						objectOffHeapPoolList.remove(oldObjectOffHeapPool);
					}
					objectOffHeapPoolList.add(objectOffHeapPool);
				}
			}
		}
		return objectOffHeapPool;
	}

	@Override
	public <T> T newObject(Class<T> objectType) {
		checkEnable();
		
		ObjectOffHeapPool<T, ?> objectOffHeapPool = getObjectOffHeapPoolSynchronized(objectType);
		
		return objectOffHeapPool.get();
	}
	
	@Override
	public <T> long newObjectAsAddress(Class<T> objectType) {
		checkEnable();
		
		ObjectOffHeapPool<T, ?> objectOffHeapPool = getObjectOffHeapPoolSynchronized(objectType);
		
		return objectOffHeapPool.getAsAddress();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> boolean freeObject(T obj) {
		checkEnable();
		
		for (int i = 0; i < objectOffHeapPoolList.size(); i++) {	
			ObjectOffHeapPool objectOffHeapPool = objectOffHeapPoolList.get(i);
			if (objectOffHeapPool.free(obj)) {
				if (objectOffHeapPool.isEmpty()) {
					objectOffHeapPoolList.remove(objectOffHeapPool);
				}
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean freeObjectWithAddress(long address) {
		checkEnable();
		
		for (int i = 0; i < objectOffHeapPoolList.size(); i++) {	
			ObjectOffHeapPool objectOffHeapPool = objectOffHeapPoolList.get(i);
			if (objectOffHeapPool.freeFromAddress(address)) {
				if (objectOffHeapPool.isEmpty()) {
					objectOffHeapPoolList.remove(objectOffHeapPool);
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public <A> A newArray(Class<A> arrayType, int length) {
		return newArray(arrayType, length, false);
	}
	
	@Override
	public <A> long newArrayAsAddress(Class<A> arrayType, int length) {
		return newArrayAsAddress(arrayType, length, false);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public synchronized <A> A newArray(Class<A> arrayType, int length, boolean initializeElements) {
		checkEnable();
		
		ArrayOffHeapPool arrayOffHeapPool =
				defaultOffHeapPoolFactory.
					createArrayOffHeapPool(arrayType, length, initializeElements);
		arrayOffHeapPoolSet.add(arrayOffHeapPool);
		return (A) arrayOffHeapPool.getArray();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public synchronized <A> long newArrayAsAddress(Class<A> arrayType, int length, boolean initializeElements) {
		checkEnable();
		
		ArrayOffHeapPool arrayOffHeapPool =
				defaultOffHeapPoolFactory.
					createArrayOffHeapPool(arrayType, length, initializeElements);
		arrayOffHeapPoolSet.add(arrayOffHeapPool);
		return arrayOffHeapPool.getArrayAsAddress();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public synchronized <A> boolean freeArray(A array) {
		checkEnable();
		
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
		checkEnable();
		
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
	
	private void ensureStringOffHeapPoolInitialized() {
		if (stringOffHeapPool == null) {
			synchronized (this) {
				if (stringOffHeapPool == null) {
					stringOffHeapPool = 
							new ExtendableStringOffHeapPool(new DefaultStringOffHeapPool());
				}
			}
		}
	}
	
	@Override
	public String newString(String str) {
		checkEnable();
		
		ensureStringOffHeapPoolInitialized();
		
		return stringOffHeapPool.get(str);
	}
	
	@Override
	public String newString(char[] chars) {
		checkEnable();
		
		ensureStringOffHeapPoolInitialized();
		
		return stringOffHeapPool.get(chars);
	}
	
	@Override
	public String newString(char[] chars, int offset, int length) {
		checkEnable();
		
		ensureStringOffHeapPoolInitialized();
		
		return stringOffHeapPool.get(chars, offset, length);
	}
	
	@Override
	public long newStringAsAddress(String str) {
		checkEnable();
		
		ensureStringOffHeapPoolInitialized();
		
		return stringOffHeapPool.getAsAddress(str);
	}
	
	@Override
	public boolean freeString(String str) {
		checkEnable();
		
		if (stringOffHeapPool != null) {
			return stringOffHeapPool.free(str);
		}
		else {
			return false;
		}
	}
	
	@Override
	public boolean freeStringWithAddress(long address) {
		checkEnable();
		
		if (stringOffHeapPool != null) {
			return stringOffHeapPool.freeFromAddress(address);
		}
		else {
			return false;
		}
	}
	
	@Override
	public Boolean getOffHeapBoolean(boolean b) {
		checkEnable();
		
		Boolean offHeapBoolean = newObject(Boolean.class);
		directMemoryService.putBoolean(offHeapBoolean, BOOLEAN_VALUE_FIELD_OFFSET, b);
		return offHeapBoolean;
	}
	
	@Override
	public Byte getOffHeapByte(byte b) {
		checkEnable();
		
		Byte offHeapByte = newObject(Byte.class);
		directMemoryService.putByte(offHeapByte, BYTE_VALUE_FIELD_OFFSET, b);
		return offHeapByte;
	}
	
	@Override
	public Character getOffHeapCharacter(char c) {
		checkEnable();
		
		Character offHeapCharacter = newObject(Character.class);
		directMemoryService.putChar(offHeapCharacter, CHARACTER_VALUE_FIELD_OFFSET, c);
		return offHeapCharacter;
	}
	
	@Override
	public Short getOffHeapShort(short s) {
		checkEnable();
		
		Short offHeapShort = newObject(Short.class);
		directMemoryService.putShort(offHeapShort, SHORT_VALUE_FIELD_OFFSET, s);
		return offHeapShort;
	}
	
	@Override
	public Integer getOffHeapInteger(int i) {
		checkEnable();
		
		Integer offHeapInteger = newObject(Integer.class);
		directMemoryService.putInt(offHeapInteger, INTEGER_VALUE_FIELD_OFFSET, i);
		return offHeapInteger;
	}
	
	@Override
	public Float getOffHeapFloat(float f) {
		checkEnable();
		
		Float offHeapFloat = newObject(Float.class);
		directMemoryService.putFloat(offHeapFloat, FLOAT_VALUE_FIELD_OFFSET, f);
		return offHeapFloat;
	}
	
	@Override
	public Long getOffHeapLong(long l) {
		checkEnable();
		
		Long offHeapLong = newObject(Long.class);
		directMemoryService.putLong(offHeapLong, LONG_VALUE_FIELD_OFFSET, l);
		return offHeapLong;
	}
	
	@Override
	public Double getOffHeapDouble(double d) {
		checkEnable();
		
		Double offHeapDouble = newObject(Double.class);
		directMemoryService.putDouble(offHeapDouble, DOUBLE_VALUE_FIELD_OFFSET, d);
		return offHeapDouble;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> boolean isInOffHeap(T obj) {
		checkEnable();
		
		if (obj == null) {
			return false;
		}
		
		Class<T> objClass = (Class<T>) obj.getClass();
		if (objClass.equals(String.class)) {
			return stringOffHeapPool.isMine((String) obj);
		} 
		else if (objClass.isArray()) {
			for (ArrayOffHeapPool arrayOffHeapPool : arrayOffHeapPoolSet) {
				if (arrayOffHeapPool.isMe(obj)) {
					return true;
				}
			}
			return false;
		}
		else {
			ObjectOffHeapPool objectOffHeapPool = objectOffHeapPoolMap.get(objClass);
			if (objectOffHeapPool != null && objectOffHeapPool instanceof ContentAwareOffHeapPool) {
				ContentAwareOffHeapPool contentAwareOffHeapPool = (ContentAwareOffHeapPool) objectOffHeapPool;
				return contentAwareOffHeapPool.isMine(obj);
			}
			else {
				return false;
			}
		}
	}
	
}
