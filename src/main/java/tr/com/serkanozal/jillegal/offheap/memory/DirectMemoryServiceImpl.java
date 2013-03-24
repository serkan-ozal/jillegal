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

@SuppressWarnings( { "unchecked" } )
public class DirectMemoryServiceImpl implements DirectMemoryService {
	
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
    
    private static final int NR_BITS = Integer.valueOf(System.getProperty("sun.arch.data.model"));
    private static final int BYTE = 8;
    private static final int WORD = NR_BITS / BYTE;
    private static final int MIN_SIZE = 16; 
    
    private static final int ADDRESS_SHIFT_SIZE_32_BIT = 0; 
    private static final int ADDRESS_SHIFT_SIZE_64_BIT = 3; 
    
    private static final int OBJECT_HEADER_SIZE_32_BIT = 8; 
    private static final int OBJECT_HEADER_SIZE_64_BIT = 12; 
    
    private Unsafe unsafe;
    private Object[] objArray;
    private long classFieldOffset;
    private long baseOffset;
    private int addressSize;
    private int indexScale;
    private int addressShiftSize;
    private int objectHeaderSize;
    
    public DirectMemoryServiceImpl() {
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
            	classFieldOffset = CLASS_FIELD_OFFSET_IN_INSTANCE_FOR_32_BIT;
                addressShiftSize = ADDRESS_SHIFT_SIZE_32_BIT;
                objectHeaderSize = OBJECT_HEADER_SIZE_32_BIT;
                break;
                
            case SIZE_64_BIT:
            	classFieldOffset = CLASS_FIELD_OFFSET_IN_INSTANCE_FOR_64_BIT;
            	addressShiftSize = ADDRESS_SHIFT_SIZE_64_BIT;
            	objectHeaderSize = OBJECT_HEADER_SIZE_64_BIT;
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
                    
                    default:    
                        throw new AssertionError("Unsupported address size: " + addressSize); 
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
    
    public void dumpObject(Object obj, long size) {
    	for (int i = 0; i < size; i++) {
	    	System.out.print(String.format("%02x ", unsafe.getByte(obj, (long)i)));
			if ((i + 1) % 16 == 0) {
				System.out.println();
			}
    	}	
    	System.out.println();
    }
  
    @Override
    public synchronized long addressOfField(Object obj, String fieldName) throws SecurityException, NoSuchFieldException {
        long baseAddress = 0; 
        long fieldOffset = 0;
        Field field = obj.getClass().getDeclaredField(fieldName);
        if (Modifier.isStatic(field.getModifiers())) {
        	baseAddress = addressOfClass(obj);
        	fieldOffset = unsafe.staticFieldOffset(field);
        }
        else {
        	baseAddress = addressOf(obj);
        	fieldOffset = unsafe.objectFieldOffset(field);
        }
        return baseAddress + fieldOffset;
    }
    
    @Override
    public long addressOfClass(Object obj) {
    	switch (addressSize) {
	        case SIZE_32_BIT:
	            return unsafe.getInt(obj, classFieldOffset);
	            
	        case SIZE_64_BIT:
	        	return (unsafe.getLong(obj, classFieldOffset) & 0x00000000FFFFFFFFL) << addressShiftSize;   
	            
	        default:    
                throw new AssertionError("Unsupported address size: " + addressSize);     
    	}
    }
    
    @Override
    public synchronized <T> T getObject(long address) {
    	address = address >> addressShiftSize;
    	
    	switch (addressSize) {
            case SIZE_32_BIT:
                unsafe.putInt(objArray, baseOffset, (int)address);
                break;
                
            case SIZE_64_BIT:
                unsafe.putLong( objArray, baseOffset, address);
                break;    
                
            default:
                throw new AssertionError("Unsupported index scale: " + indexScale);
        }       
    	
        return (T) objArray[0];
    }
    
    @Override
    public synchronized <T> void setObject(long address, T obj) {
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
            unsafe.copyMemory(objAddress, address, objectHeaderSize + objSize);   
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
