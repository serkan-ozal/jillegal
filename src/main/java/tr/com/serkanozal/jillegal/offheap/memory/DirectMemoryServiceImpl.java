/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.memory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.agent.JillegalAgent;
import tr.com.serkanozal.jillegal.util.JvmUtil;


@SuppressWarnings( { "unchecked", "restriction" } )
public class DirectMemoryServiceImpl implements DirectMemoryService {
	
	private final Logger logger = Logger.getLogger(getClass());

    private Unsafe unsafe;
    private Object[] objArray;
    
    public DirectMemoryServiceImpl() {
    	init();
    }
    
    private void init() {
        initUnsafe();

    }
    
    private void initUnsafe() {
        unsafe = JvmUtil.getUnsafe();
        objArray = new Object[1];
    }

    @Override
    public long allocateMemory(long size) {
    	return unsafe.allocateMemory(size);
    }
    
    @Override
    public void freeMemory(long address) {
    	unsafe.freeMemory(address);
    }
    
    @Override
    public Object allocateInstance(Class<?> clazz) {
    	try {
			return unsafe.allocateInstance(clazz);
		} 
    	catch (InstantiationException e) {
    		logger.error("Error at UnsafeBasedOffHeapMemoryServiceImpl.allocateInstance()", e);
    		return null;
		}
    }
    
    @Override
    public void copyMemory(long sourceAddress, long destinationAddress, long size) {
    	unsafe.copyMemory(sourceAddress, destinationAddress, size);
    }
   
    public long sizeOfWithAgent(Object obj) {
        return JillegalAgent.sizeOf(obj);
    }
    
    public long sizeOfWithUnsafe(Object obj) {
        if (obj == null) {
            return 0;
        }    
        else {
        	long classAddress = addressOfClass(obj.getClass());
            switch (JvmUtil.getAddressSize()) {
                case JvmUtil.SIZE_32_BIT:
                    return unsafe.getInt(classAddress + JvmUtil.getSizeFieldOffsetOffsetInClass());  
                case JvmUtil.SIZE_64_BIT:
                    return unsafe.getInt(classAddress + JvmUtil.getSizeFieldOffsetOffsetInClass());  
                default:
                    throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());    
            }
        }    
    }  
    
    public long sizeOfWithReflection(Class<?> objClass) {
    	List<Field> instanceFields = new LinkedList<Field>();
    	
        do {
            if (objClass == Object.class) {
            	return JvmUtil.MIN_SIZE;
            }
            for (Field f : objClass.getDeclaredFields()) {
                if ((f.getModifiers() & Modifier.STATIC) == 0) {
                    instanceFields.add(f);
                }    
            }
            
            objClass = objClass.getSuperclass();
        } while (instanceFields.isEmpty());

        long maxOffset = 0;
        for (Field f : instanceFields) {
            long offset = unsafe.objectFieldOffset(f);
            if (offset > maxOffset) {
            	maxOffset = offset; 
            }	
        }
        return (((long) maxOffset / JvmUtil.WORD) + 1) * JvmUtil.WORD; 
    }

    @Override
    public long sizeOf(Class<?> objClass) {
    	return sizeOfWithReflection(objClass);
    }
    
    @Override
    public synchronized long addressOf(Object obj) {
        if (obj == null) {
            return 0;
        }
        
        objArray[0] = obj;
        long objectAddress = JvmUtil.INVALID_ADDRESS;
        
        switch (JvmUtil.getIndexScale()) {
            case JvmUtil.SIZE_32_BIT:
            case JvmUtil.SIZE_64_BIT:
                switch (JvmUtil.getAddressSize()) {
                    case JvmUtil.SIZE_32_BIT:
                        objectAddress = unsafe.getInt(objArray, JvmUtil.getBaseOffset());
                        break;
                    case JvmUtil.SIZE_64_BIT:
                        objectAddress = unsafe.getLong(objArray, JvmUtil.getBaseOffset());
                        break;    
                    default:    
                        throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize()); 
                }
                break; 

            default:
                throw new AssertionError("Unsupported index scale: " + JvmUtil.getIndexScale());
        }       

        if (objectAddress != JvmUtil.INVALID_ADDRESS) {
        	objectAddress = JvmUtil.toNativeAddress(objectAddress);
        }
        
        return objectAddress;
    }
  
    @Override
    public synchronized long addressOfField(Object obj, String fieldName) throws SecurityException, NoSuchFieldException {
        long baseAddress = 0; 
        long fieldOffset = 0;
        Field field = obj.getClass().getDeclaredField(fieldName);
        if (Modifier.isStatic(field.getModifiers())) {
        	baseAddress = addressOfClass(obj.getClass());
        	fieldOffset = unsafe.staticFieldOffset(field);
        }
        else {
        	baseAddress = addressOf(obj);
        	fieldOffset = unsafe.objectFieldOffset(field);
        }
        return baseAddress + fieldOffset;
    }
    
    @Override
    public long addressOfClass(Class<?> clazz) {
    	long addressOfClass = addressOf(clazz);
    	switch (JvmUtil.getAddressSize()) {
	        case JvmUtil.SIZE_32_BIT:
	            return JvmUtil.toNativeAddress(unsafe.getInt(addressOfClass + JvmUtil.getClassDefPointerOffsetInClass()));
	        case JvmUtil.SIZE_64_BIT:
	        	return JvmUtil.toNativeAddress(unsafe.getLong(addressOfClass + JvmUtil.getClassDefPointerOffsetInClass()));   
	        default:    
                throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());     
    	}
    }
    
    @Override
    public synchronized <T> T getObject(long address) {
    	address = JvmUtil.toJvmAddress(address);
    	
    	switch (JvmUtil.getAddressSize()) {
            case JvmUtil.SIZE_32_BIT:
                unsafe.putInt(objArray, JvmUtil.getBaseOffset(), (int)address);
                break;
            case JvmUtil.SIZE_64_BIT:
                unsafe.putLong(objArray, JvmUtil.getBaseOffset(), address);
                break;    
            default:
                throw new AssertionError("Unsupported index size: " + JvmUtil.getAddressSize());
        }       
    	
        return (T) objArray[0];
    }
    
    @Override
    public synchronized <T> void setObject(long address, T obj) {
        if (obj == null) {
            switch (JvmUtil.getAddressSize()) {
                case JvmUtil.SIZE_32_BIT:
                    unsafe.putInt(address, 0);
                    break;
                case JvmUtil.SIZE_64_BIT:
                    unsafe.putLong(address, 0L);
                    break;    
                default:
                    throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());
            }
        }
        else {
            long objSize = sizeOf(obj.getClass());
            long objAddress = addressOf(obj);
            unsafe.copyMemory(objAddress, address, JvmUtil.getHeaderSize() + objSize);   
        }    
    }
    
    @Override
    public synchronized <T> T changeObject(T source, T target) {
        if (source == null) {
            throw new IllegalArgumentException("Source object is null !");
        }
        long targetAddress = addressOf(target);
        setObject(targetAddress, source);
        
        return target;
    }
    
    @Override
    public synchronized <T> T copyObject(T original) {
    	if (original == null) {
            throw new IllegalArgumentException("Original object is null !");
        }
        long originalAddress = addressOf(original);
        Object[] array = new Object[] {null};
        
        switch (JvmUtil.getAddressSize()) {
	        case JvmUtil.SIZE_32_BIT:
	        	unsafe.putInt(array, JvmUtil.getBaseOffset(), (int)originalAddress);
	            break;
	        case JvmUtil.SIZE_64_BIT:
	        	unsafe.putLong(array, JvmUtil.getBaseOffset(), originalAddress);
	            break;    
	        default:
	            throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());
        }

        return (T) array[0];
    }
    
    @Override
    public byte getByte(long address) {
    	return unsafe.getByte(address);
    }

    @Override
    public void putByte(long address, byte x) {
    	unsafe.putByte(address, x);
    }
    
    @Override
    public char getChar(long address) {
    	return unsafe.getChar(address);
    }

    @Override
    public void putChar(long address, char x) {
    	unsafe.putChar(address, x);
    }

    @Override
    public short getShort(long address) {
    	return unsafe.getShort(address);
    }

    @Override
    public void putShort(long address, short x) {
    	unsafe.putShort(address, x);
    }
   
    @Override
    public int getInt(long address) {
    	return unsafe.getInt(address);
    }

    @Override
    public void putInt(long address, int x) {
    	unsafe.putInt(address, x);
    }
    
    @Override
    public float getFloat(long address) {
    	return unsafe.getFloat(address);
    }

    @Override
    public void putFloat(long address, float x) {
    	unsafe.putFloat(address, x);
    }

    @Override
    public long getLong(long address) {
    	return unsafe.getLong(address);
    }

    @Override
    public void putLong(long address, long x) {
    	unsafe.putLong(address, x);
    }

    @Override
    public double getDouble(long address) {
    	return unsafe.getDouble(address);
    }

    @Override
    public void putDouble(long address, double x) {
    	unsafe.putDouble(address, x);
    }

    @Override
    public long getAddress(long address) {
    	return unsafe.getAddress(address);
    }

    @Override
    public void putAddress(long address, long x) {
    	unsafe.putAddress(address, x);
    }
    
    @Override
    public boolean getBoolean(Object o, long offset) {
    	return unsafe.getBoolean(o, offset);
    }
    
    @Override
    public void putBoolean(Object o, long offset, boolean x) {
    	unsafe.putBoolean(o, offset, x);
    }
    
    @Override
    public byte getByte(Object o, long offset) {
    	return unsafe.getByte(o, offset);
    }
   
    @Override
    public void putByte(Object o, long offset, byte x) {
    	unsafe.putByte(o, offset, x);
    }
    
    @Override
    public char getChar(Object o, long offset) {
    	return unsafe.getChar(o, offset);
    }
    
    @Override
    public void putChar(Object o, long offset, char x) {
    	unsafe.putChar(o, offset, x);
    }
   
    @Override
    public short getShort(Object o, long offset) {
    	return unsafe.getShort(o, offset);
    }
    
    @Override
    public void putShort(Object o, long offset, short x) {
    	unsafe.putShort(o, offset, x);
    }
    
    @Override
    public int getInt(Object o, long offset) {
    	return unsafe.getInt(o, offset);
    }

    @Override
    public void putInt(Object o, long offset, int x) {
    	unsafe.putInt(o, offset, x);
    }

    @Override
    public float getFloat(Object o, long offset) {
    	return unsafe.getFloat(o, offset);
    }
    
    @Override
    public void putFloat(Object o, long offset, float x) {
    	unsafe.putFloat(o, offset, x);
    }
    
    @Override
    public long getLong(Object o, long offset) {
    	return unsafe.getLong(o, offset);
    }
    
    @Override
    public void putLong(Object o, long offset, long x) {
    	unsafe.putLong(o, offset, x);
    }
   
    @Override
    public double getDouble(Object o, long offset) {
    	return unsafe.getDouble(o, offset);
    }
    
    @Override
    public void putDouble(Object o, long offset, double x) {
    	unsafe.putDouble(o, offset, x);
    }
    
    @Override
    public Object getObject(Object o, long offset) {
    	return unsafe.getInt(o, offset);
    }

    @Override
    public void putObject(Object o, long offset, Object x) {
    	unsafe.putObject(o, offset, x);
    }

}
