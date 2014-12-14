/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.memory;

import org.apache.log4j.Logger;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.agent.JillegalAgent;
import tr.com.serkanozal.jillegal.offheap.memory.allocator.MemoryAllocator;
import tr.com.serkanozal.jillegal.offheap.memory.allocator.MemoryAllocatorFactory;
import tr.com.serkanozal.jillegal.util.JvmUtil;

@SuppressWarnings( { "unchecked", "restriction" } )
public class DirectMemoryServiceImpl implements DirectMemoryService {
	
	private final Logger logger = Logger.getLogger(getClass());

    private Unsafe unsafe;
    private Object[] objArray;
    private MemoryAllocator memoryAllocator;
    
    public DirectMemoryServiceImpl() {
    	init();
    }
    
    private void init() {
        initUnsafe();
    }
    
    private void initUnsafe() {
        unsafe = JvmUtil.getUnsafe();
        objArray = new Object[1];
        memoryAllocator = MemoryAllocatorFactory.getDefaultMemoryAllocator();
    }

    @Override
    public long allocateMemory(long size) {
    	return memoryAllocator.allocateMemory(size);
    }
    
    @Override
    public void freeMemory(long address) {
    	memoryAllocator.freeMemory(address);
    }
    
    @Override
    public <T> void freeObject(T obj) {
    	if (obj != null) {
    		memoryAllocator.freeMemory(JvmUtil.addressOf(obj));
    	}
    }
    
    @Override
    public Object allocateInstance(Class<?> clazz) {
    	try {
			return unsafe.allocateInstance(clazz);
		} 
    	catch (InstantiationException e) {
    		logger.error("Error at DirectMemoryServiceImpl.allocateInstance()", e);
    		return null;
		}
    }
    
    @Override
    public void copyMemory(long sourceAddress, long destinationAddress, long size) {
    	unsafe.copyMemory(sourceAddress, destinationAddress, size);
    }
    
    @Override
    public void setMemory(long sourceAddress, long bytes, byte val) {
    	unsafe.setMemory(sourceAddress, bytes, val);
    }

    public long sizeOfWithAgent(Object obj) {
        return JillegalAgent.sizeOf(obj);
    }
    
    public long sizeOfWithUnsafe(Object obj) {
    	return JvmUtil.sizeOfWithUnsafe(obj); 
    }  
    
    public long sizeOfWithReflection(Class<?> objClass) {
    	return JvmUtil.sizeOfWithReflection(objClass);
    }

    @Override
    public <T> long sizeOfObject(T obj) {
    	return sizeOfWithAgent(obj);
    }
    
    @Override
    public long sizeOfClass(Class<?> objClass) {
    	return sizeOfWithReflection(objClass);
    }
    
    @Override
    public synchronized long addressOf(Object obj) {
        return JvmUtil.addressOf(obj);
    }
  
    @Override
    public synchronized long addressOfField(Object obj, String fieldName) throws SecurityException, NoSuchFieldException {
        return JvmUtil.addressOfField(obj, fieldName);
    }
    
    @Override
    public long addressOfClass(Class<?> clazz) {
    	return JvmUtil.addressOfClass(clazz);
    }
    
    @Override
    public <O, F> F getObjectField(O obj, int fieldOffset) {
    	// synchronized (obj) {
    		long objAddress = JvmUtil.addressOf(obj);
        	long fieldAddress = objAddress + fieldOffset;
        	if (fieldAddress != 0 && fieldAddress != JvmUtil.INVALID_ADDRESS) {
        		return getObject(fieldAddress);
        	}
        	else {
        		return null;
        	}
		// }
    }
    
    @Override
    public <O, F> void setObjectField(O rootObj, int fieldOffset, F fieldObj) {
    	// synchronized (rootObj) {
    		long objAddress = JvmUtil.addressOf(rootObj);
	    	long fieldAddress = objAddress + fieldOffset;
	    	if (fieldObj == null) {
	    		int referenceSize = JvmUtil.getReferenceSize();
            	switch (referenceSize) {
                 	case JvmUtil.ADDRESSING_4_BYTE:   
                 		unsafe.putInt(fieldAddress, (int) JvmUtil.NULL);
                 		break;
                 	case JvmUtil.ADDRESSING_8_BYTE:
                 		unsafe.putLong(fieldAddress, JvmUtil.NULL);
                 		break;	
                 	default:    
                        throw new AssertionError("Unsupported reference size: " + referenceSize);
            	}
	    	}
	    	else {
	    		// synchronized (fieldObj) {
	    			long fieldObjAddress = JvmUtil.toJvmAddress(JvmUtil.addressOf(fieldObj));
	    	      	if (fieldAddress != 0 && fieldAddress != JvmUtil.INVALID_ADDRESS) {
	    	    		switch (JvmUtil.getAddressSize()) {
	    		            case JvmUtil.SIZE_32_BIT:
	    		            	putAsIntAddress(fieldAddress, fieldObjAddress);
	    		                break;
	    		            case JvmUtil.SIZE_64_BIT:
	    		            	int referenceSize = JvmUtil.getReferenceSize();
	    		            	switch (referenceSize) {
	    		                 	case JvmUtil.ADDRESSING_4_BYTE:   
	    		                 		putAsIntAddress(fieldAddress, fieldObjAddress);
	    		                 		break;
	    		                 	case JvmUtil.ADDRESSING_8_BYTE:
	    		                 		unsafe.putLong(fieldAddress, fieldObjAddress);
	    		                 		break;	
	    		                 	default:    
	    		                        throw new AssertionError("Unsupported reference size: " + referenceSize);
	    		            	}
	    		            	break; 
	    		            default:
	    		                throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());
	    	    		}      
	    	    	}	
				// }
	    	}
    	// }	
    }
    
    @Override
    public <O, F> F getObjectField(O obj, String fieldName) {
    	// synchronized (obj) {
    		long fieldAddress = JvmUtil.addressOfField(obj, fieldName);
        	if (fieldAddress != 0 && fieldAddress != JvmUtil.INVALID_ADDRESS) {
        		return getObject(fieldAddress);
        	}
        	else {
        		return null;
        	}
		// }
    }
    
    @Override
    public <O, F> void setObjectField(O rootObj, String fieldName, F fieldObj) {
    	// synchronized (rootObj) {
    		long fieldAddress = JvmUtil.addressOfField(rootObj, fieldName);
	    	if (fieldObj == null) {
	    		int referenceSize = JvmUtil.getReferenceSize();
            	switch (referenceSize) {
                 	case JvmUtil.ADDRESSING_4_BYTE:   
                 		unsafe.putInt(fieldAddress, (int) JvmUtil.NULL);
                 		break;
                 	case JvmUtil.ADDRESSING_8_BYTE:
                 		unsafe.putLong(fieldAddress, JvmUtil.NULL);
                 		break;	
                 	default:    
                        throw new AssertionError("Unsupported reference size: " + referenceSize);
            	}
	    	}
	    	else {
	    		// synchronized (fieldObj) {
	    			long fieldObjAddress = JvmUtil.toJvmAddress(JvmUtil.addressOf(fieldObj));
			    	if (fieldAddress != 0 && fieldAddress != JvmUtil.INVALID_ADDRESS) {
			    		switch (JvmUtil.getAddressSize()) {
				            case JvmUtil.SIZE_32_BIT:
				            	putAsIntAddress(fieldAddress, fieldObjAddress);
				                break;
				            case JvmUtil.SIZE_64_BIT:
				            	int referenceSize = JvmUtil.getReferenceSize();
				            	switch (referenceSize) {
				                 	case JvmUtil.ADDRESSING_4_BYTE:   
				                 		putAsIntAddress(fieldAddress, fieldObjAddress);
				                 		break;
				                 	case JvmUtil.ADDRESSING_8_BYTE:
				                 		unsafe.putLong(fieldAddress, fieldObjAddress);
				                 		break;
				                 	default:    
				                        throw new AssertionError("Unsupported reference size: " + referenceSize);
				            	}
				            	break; 
				            default:
				                throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());
			    		}      
			    	}
	    		// }
	    	}	
		// }
    }
    
    @Override
    public Object getArrayElement(Object array, int elementIndex) {
    	// synchronized (array) {
    		long arrayElementAddress = JvmUtil.getArrayElementAddress(array, elementIndex);
        	if (arrayElementAddress != 0 && arrayElementAddress != JvmUtil.INVALID_ADDRESS) {
        		return getObject(arrayElementAddress);
        	}
        	else {
        		return null;
        	}
		// } 
    }
    
    @Override
    public void setArrayElement(Object array, int elementIndex, Object element) {
    	// synchronized (array) {
	    	long arrayElementAddress = JvmUtil.getArrayElementAddress(array, elementIndex);
	    	long elementAddress = JvmUtil.toJvmAddress(JvmUtil.addressOf(element));
	    	if (elementAddress != JvmUtil.INVALID_ADDRESS) {
	    		switch (JvmUtil.getAddressSize()) {
		            case JvmUtil.SIZE_32_BIT:
		            	putAsIntAddress(arrayElementAddress, elementAddress);
		                break;
		            case JvmUtil.SIZE_64_BIT:
		            	int referenceSize = JvmUtil.getReferenceSize();
		            	switch (referenceSize) {
		                 	case JvmUtil.ADDRESSING_4_BYTE:   
		                 		putAsIntAddress(arrayElementAddress, elementAddress);
		                 		break;
		                 	case JvmUtil.ADDRESSING_8_BYTE:
		                 		unsafe.putLong(arrayElementAddress, elementAddress);
		                 		break;
		            	}
		            	break; 
	    		}      
	    	}
    	// }	
    }
    
    @Override
    public synchronized <T> T getObject(long address) {
    	address = JvmUtil.toJvmAddress(address);
    	switch (JvmUtil.getAddressSize()) {
            case JvmUtil.SIZE_32_BIT:
                putAsIntAddress(objArray, JvmUtil.getBaseOffset(), address);
                break;
            case JvmUtil.SIZE_64_BIT:
            	int referenceSize = JvmUtil.getReferenceSize();
            	switch (referenceSize) {
                 	case JvmUtil.ADDRESSING_4_BYTE:
                 		putAsIntAddress(objArray, JvmUtil.getBaseOffset(), address);
                 		break;
                 	case JvmUtil.ADDRESSING_8_BYTE:
                 		unsafe.putLong(objArray, JvmUtil.getBaseOffset(), address);
                 		break;
            	}
            	break; 
        }       
    	T obj = (T) unsafe.getObject(objArray, JvmUtil.getBaseOffset()); 
    	unsafe.putObject(objArray, JvmUtil.getBaseOffset(), null); 
        return obj;
    }
    
	@Override
    public synchronized <T> void setObject(long address, T obj) {
        if (obj == null) {
            switch (JvmUtil.getAddressSize()) {
                case JvmUtil.SIZE_32_BIT:
                    unsafe.putInt(address, 0);
                    break;
                case JvmUtil.SIZE_64_BIT:
                	int referenceSize = JvmUtil.getReferenceSize();
                	switch (referenceSize) {
                     	case JvmUtil.ADDRESSING_4_BYTE:
                     		unsafe.putInt(address, 0);
                     		break;
                     	case JvmUtil.ADDRESSING_8_BYTE:
                     		unsafe.putLong(address, 0L);
                     		break;
                	}
                    break;
            }
        }
        else {
        	// synchronized (obj) {
                long objSize = sizeOfClass(obj.getClass());
                long objAddress = addressOf(obj);
                unsafe.copyMemory(objAddress, address, JvmUtil.getHeaderSize() + objSize);   
             // } 
        }    
    }
    
    @Override
    public synchronized <T> T changeObject(T source, T target) {
        if (source == null) {
            throw new IllegalArgumentException("Source object is null !");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target object is null !");
        }
        // synchronized (source) {
        	// synchronized (target) {
                long targetAddress = addressOf(target);
                setObject(targetAddress, source);
                return target;
    		// }
        // }
    }
    
    @Override
    public synchronized <T> T copyObject(T original) {
    	if (original == null) {
            throw new IllegalArgumentException("Original object is null !");
        }
    	// synchronized (original) {
    		long originalAddress = addressOf(original);
            Object[] array = new Object[] { null };
            switch (JvmUtil.getAddressSize()) {
    	        case JvmUtil.SIZE_32_BIT:
    	        	putAsIntAddress(array, JvmUtil.getBaseOffset(), originalAddress);
    	            break;
    	        case JvmUtil.SIZE_64_BIT:
    	        	int referenceSize = JvmUtil.getReferenceSize();
                	switch (referenceSize) {
                     	case JvmUtil.ADDRESSING_4_BYTE:
                     		putAsIntAddress(array, JvmUtil.getBaseOffset(), originalAddress);
                     		break;
                     	case JvmUtil.ADDRESSING_8_BYTE:
                     		unsafe.putLong(array, JvmUtil.getBaseOffset(), originalAddress);
                     		break;
                	}
    	            break;  
            }
            return (T) array[0];
		// } 
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
    	return unsafe.getObject(o, offset);
    }

    @Override
    public void putObject(Object o, long offset, Object x) {
    	unsafe.putObject(o, offset, x);
    }
    
    @Override
    public long getAsIntAddress(long address) {
    	return JvmUtil.normalize(unsafe.getInt(address));
    }
    
    @Override
    public long getAsIntAddress(Object obj, long offset) {
    	return JvmUtil.normalize(unsafe.getInt(obj, offset));
    }
    
    @Override
    public void putAsIntAddress(long address, long intAddress) {
    	long l = unsafe.getLong(address + JvmUtil.INT_SIZE);
 		unsafe.putLong(address, intAddress);
 		unsafe.putLong(address + JvmUtil.INT_SIZE, l);
    }
    
    @Override
    public void putAsIntAddress(Object obj, long offset, long intAddress) {
    	long l = unsafe.getLong(obj, offset + JvmUtil.INT_SIZE);
 		unsafe.putLong(obj, offset, intAddress);
 		unsafe.putLong(obj, offset + JvmUtil.INT_SIZE, l);
    }

}
