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

@SuppressWarnings( { "unchecked", "restriction" } )
public class UnsafeBasedOffHeapMemoryServiceImpl implements OffHeapMemoryService {
	
	private final Logger logger = Logger.getLogger(getClass());
	
    public static final byte SIZE_32_BIT = 4;
    public static final byte SIZE_64_BIT = 8;
    public static final byte INVALID_ADDRESS = -1;
    /**
     * @link http://hg.openjdk.java.net/jdk7/hotspot/hotspot/file/9b0ca45cd756/src/share/vm/oops/oop.hpp
     */
    public static final long CLASS_FIELD_OFFSET_IN_INSTANCE_FOR_32_BIT = 4L;
    public static final long CLASS_FIELD_OFFSET_IN_INSTANCE_FOR_64_BIT = 8L;
    /**
     * @link http://hg.openjdk.java.net/jdk7/hotspot/hotspot/file/9b0ca45cd756/src/share/vm/oops/klass.hpp
     */
    public static final long SIZE_FIELD_OFFSET_IN_CLASS = 12L;
    
    public static final long CLASS_HEADER_SIZE = 8;
    private static final int NR_BITS = Integer.valueOf(System.getProperty("sun.arch.data.model"));
    private static final int BYTE = 8;
    private static final int WORD = NR_BITS / BYTE;
    private static final int MIN_SIZE = 16; 
    
    private static final int ADDRESS_SHIFT_SIZE_32_BIT = 0; 
    private static final int ADDRESS_SHIFT_SIZE_64_BIT = 3; 
    
    private Unsafe unsafe;
    private Object[] objArray;
    private long baseOffset;
    private int addressSize;
    private int indexScale;
    private int addressShiftSize;
    
    public UnsafeBasedOffHeapMemoryServiceImpl() {
    	init();
    }
    
    private void init() {
        initUnsafe();
        
        objArray = new Object[1];
        baseOffset = unsafe.arrayBaseOffset(Object[].class);
        indexScale = unsafe.arrayIndexScale(Object[].class);
        addressSize = unsafe.addressSize();
        
        switch (addressSize) {
            case SIZE_32_BIT:
                addressShiftSize = ADDRESS_SHIFT_SIZE_32_BIT;
                break;
                
            case SIZE_64_BIT:
            	addressShiftSize = ADDRESS_SHIFT_SIZE_64_BIT;
                break;    
        }        
    }
    
    private void initUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        }
        catch (Exception e) {
        	logger.error("Error at UnsafeBasedOffHeapMemoryServiceImpl.initUnsafe()", e);
        }
    }
    
    public Unsafe getUnsafe() {
        return unsafe;
    }
    
    public void info() {
        System.out.println("Unsafe: " + unsafe );
        System.out.println("\tAddressSize : " + unsafe.addressSize() );
        System.out.println("\tPage Size   : " + unsafe.pageSize());
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
    
    public long normalize(int value) {
        if (value >= 0) {
            return value;
        }    
        else {
            return (~0L >>> 32) & value;
        }    
    }
    
    public long sizeOfWithAgent(Object obj) {
        return JillegalAgent.sizeOf(obj);
    }
    
    /*
    TODO Fix and uncomment
    public static long sizeOfWithUnsafe(Object obj) {
        if (obj == null) {
            return 0;
        }    
        else {
            switch (addressSize) {
                case SIZE_32_BIT:
                    return
                    	unsafe.getAddress( 
                                normalize(unsafe.getInt(obj, CLASS_FIELD_OFFSET_IN_INSTANCE_FOR_32_BIT)) + SIZE_FIELD_OFFSET_IN_CLASS);  
                    
                case SIZE_64_BIT:
                    return
                        unsafe.getAddress(
                        		normalize(unsafe.getInt(obj, CLASS_FIELD_OFFSET_IN_INSTANCE_FOR_64_BIT)) + SIZE_FIELD_OFFSET_IN_CLASS);  
                    
                default:
                    throw new AssertionError("Unsupported address size: " + addressSize);    
            }
        }    
    }  
    */
    
    public long sizeOfWithReflection(Class<?> objClass) {
    	List<Field> instanceFields = new LinkedList<Field>();
    	
        do {
            if (objClass == Object.class) {
            	return MIN_SIZE;
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
        return (((long) maxOffset / WORD) + 1) * WORD; 
    }	
    
    @Override
    public long sizeOf(Class<?> objClass) {
    	return sizeOfWithReflection(objClass);
    }
    
    @Override
    public long internalAddressOf(Object obj) {
        return normalize(System.identityHashCode(obj));
    }
    
    @Override
    public synchronized long addressOf(Object obj) {
        if (obj == null) {
            return 0;
        }
        
        objArray[0] = obj;
        long objectAddress = INVALID_ADDRESS;
        
        switch (indexScale) {
            case SIZE_32_BIT:
            case SIZE_64_BIT:
                switch (addressSize) {
                    case SIZE_32_BIT:
                        objectAddress = unsafe.getInt(objArray, baseOffset);
                        break;
                        
                    case SIZE_64_BIT:
                        objectAddress = unsafe.getLong(objArray, baseOffset);
                        break;    
                }
                break; 

            default:
                throw new AssertionError("Unsupported index scale: " + indexScale);
        }       

        if (objectAddress != INVALID_ADDRESS) {
        	objectAddress = objectAddress << addressShiftSize;
        }
        
        return objectAddress;
    }
  
    @Override
    public synchronized long addressOfField(Object obj, String fieldName) throws SecurityException, NoSuchFieldException {
        long objectAddress = addressOf(obj);
        Field field = obj.getClass().getDeclaredField(fieldName);
        long fieldOffset = unsafe.objectFieldOffset(field);
        
        return objectAddress + fieldOffset;
    }
    
    @Override
    public synchronized <T> T getObject(long address) {
    	address = address >> addressShiftSize;
    	
    	switch (addressSize) {
            case SIZE_32_BIT:
                unsafe.putInt(objArray, baseOffset, (int)address);
                break;
                
            case SIZE_64_BIT:
                unsafe.putLong( objArray, baseOffset, address );
                break;    
                
            default:
                throw new AssertionError("Unsupported index scale: " + indexScale);
        }       
    	
        return (T) objArray[0];
    }
    
    @Override
    public synchronized <T> void setObject(long address, T obj) {
    	address = address >> addressShiftSize;
    	
        if (obj == null) {
            switch (addressSize) {
                case SIZE_32_BIT:
                    unsafe.putInt(address, 0);
                    break;
                    
                case SIZE_64_BIT:
                    unsafe.putLong(address, 0L);
                    break;    
                    
                default:
                    throw new AssertionError("Unsupported address size: " + addressSize);
            }
        }
        else {
            long objSize = sizeOf(obj.getClass());
            long objAddress = addressOf(obj);
            unsafe.copyMemory(objAddress, address, objSize);   
        }    
    }
    
    @Override
    public synchronized <T> T changeObject(T source, T target) {
        if (source == null) {
            throw new IllegalArgumentException("Source object is null !");
        }
        long sourceAddress = addressOf(source);
        setObject(sourceAddress, target);
        
        return target;
    }
    
}
