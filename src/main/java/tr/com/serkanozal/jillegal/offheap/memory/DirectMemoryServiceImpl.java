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
    private ThreadLocal<Object[]> objArrayBuffers;
    private MemoryAllocator memoryAllocator;
    
    public DirectMemoryServiceImpl() {
    	init();
    }
    
    private void init() {
        initUnsafe();
    }
    
    private void initUnsafe() {
        unsafe = JvmUtil.getUnsafe();
        objArrayBuffers = new ThreadLocal<Object[]>() {
            @Override
            protected Object[] initialValue() {
                return new Object[1];
            }
        };
        memoryAllocator = MemoryAllocatorFactory.getDefaultMemoryAllocator();
    }

    @Override
    public MemoryAllocator getMemoryAllocator() {
    	return memoryAllocator;
    }
    
    @Override
    public void setMemoryAllocator(MemoryAllocator memoryAllocator) {
    	this.memoryAllocator = memoryAllocator;
    }
    
    @Override
    public long allocateMemory(long size) {
    	long address = memoryAllocator.allocateMemory(size);
    	if (address > 0) {
    		unsafe.setMemory(address, size, (byte) 0x00);
    	}
    	return address;
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
    public void copyMemory(Object sourceObject, long sourceOffset, Object destinationObject, 
    		long destinationOffset, long size) {
    	unsafe.copyMemory(sourceObject, sourceOffset, destinationObject, destinationOffset, size);
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
    public long addressOf(Object obj) {
        return JvmUtil.addressOf(obj);
    }
  
    @Override
    public long addressOfField(Object obj, String fieldName) throws SecurityException, NoSuchFieldException {
        return JvmUtil.addressOfField(obj, fieldName);
    }
    
    @Override
    public long addressOfClass(Class<?> clazz) {
    	return JvmUtil.addressOfClass(clazz);
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public <O, F> F getObjectField(O obj, int fieldOffset) {
    	if (obj == null) {
    		return null;
    	}
//    	synchronized (obj) {
//    		long objAddress = JvmUtil.addressOf(obj);
//        	long fieldAddress = objAddress + fieldOffset;
//        	long pointedAddress = JvmUtil.toNativeAddress(unsafe.getAddress(fieldAddress));
//        	if (pointedAddress != JvmUtil.NULL && pointedAddress != JvmUtil.INVALID_ADDRESS) {
//        		return getObject(pointedAddress);
//        	}
//        	else {
//        		return null;
//        	}
//		}
    	return (F) unsafe.getObject(obj, fieldOffset);
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public <O, F> void setObjectField(O rootObj, int fieldOffset, F fieldObj) {
    	if (rootObj == null) {
    		return;
    	}
//    	synchronized (rootObj) {
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
//	    		synchronized (fieldObj) {
	    			long fieldObjAddress = JvmUtil.toJvmAddress(JvmUtil.addressOf(fieldObj));
	    	      	if (fieldAddress != JvmUtil.NULL && fieldAddress != JvmUtil.INVALID_ADDRESS) {
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
//				}
	    	}
//    	}	
//    	System.out.print("set object field >>> ");
//    	System.out.println(
//    			"rootObj class: " + rootObj.getClass() + ", " +
//    			"rootObj header: " + unsafe.getLong(rootObj, 0L) + ", " +
//    			"rootObj: " + JvmUtil.toHexAddress(addressOf(rootObj)) + ", " +
//    			"fieldOffset: " + JvmUtil.toHexAddress(fieldOffset) + ", " + 
//    			"fieldObj: " + JvmUtil.toHexAddress(addressOf(fieldObj)));
/*    	
//    	synchronized (rootObj) {
	    	if (fieldObj == null) {
	    		int referenceSize = JvmUtil.getReferenceSize();
            	switch (referenceSize) {
                 	case JvmUtil.ADDRESSING_4_BYTE:   
                 		unsafe.putInt(rootObj, fieldOffset, (int) JvmUtil.NULL);
                 		break;
                 	case JvmUtil.ADDRESSING_8_BYTE:
                 		unsafe.putLong(rootObj, fieldOffset, JvmUtil.NULL);
                 		break;	
                 	default:    
                        throw new AssertionError("Unsupported reference size: " + referenceSize);
            	}
	    	}
	    	else {
//	    		synchronized (fieldObj) {
	    			long fieldObjAddress = JvmUtil.toJvmAddress(addressOf(fieldObj));
	    			switch (JvmUtil.getAddressSize()) {
			            case JvmUtil.SIZE_32_BIT:
			            	putAsIntAddress(rootObj, fieldOffset, fieldObjAddress);
			                break;
			            case JvmUtil.SIZE_64_BIT:
			            	int referenceSize = JvmUtil.getReferenceSize();
			            	switch (referenceSize) {
			                 	case JvmUtil.ADDRESSING_4_BYTE:   
			                 		putAsIntAddress(rootObj, fieldOffset, fieldObjAddress);
			                 		break;
			                 	case JvmUtil.ADDRESSING_8_BYTE:
			                 		unsafe.putLong(rootObj, fieldOffset, fieldObjAddress);
			                 		break;	
			                 	default:    
			                        throw new AssertionError("Unsupported reference size: " + referenceSize);
			            	}
			            	break; 
			            default:
			                throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());
		    		}      	
//				}
	    	}
//    	}
	*/
    }
    
    @Override
    public <O, F> F getObjectField(O obj, String fieldName) {
    	if (obj == null) {
    		return null;
    	}
//    	synchronized (obj) {
//    		long fieldAddress = JvmUtil.addressOfField(obj, fieldName);
//    		long pointedAddress = JvmUtil.toNativeAddress(unsafe.getAddress(fieldAddress));
//        	if (pointedAddress != JvmUtil.NULL && pointedAddress != JvmUtil.INVALID_ADDRESS) {
//        		return getObject(pointedAddress);
//        	}
//        	else {
//        		return null;
//        	}
//		}
    	return getObjectField(obj, (int) JvmUtil.offsetOfField(obj, fieldName));
    }
    
    @Override
    public <O, F> void setObjectField(O rootObj, String fieldName, F fieldObj) {
    	if (rootObj == null) {
    		return;
    	}
//    	synchronized (rootObj) {
    		long fieldOffset = JvmUtil.offsetOfField(rootObj, fieldName);
	    	if (fieldObj == null) {
	    		int referenceSize = JvmUtil.getReferenceSize();
            	switch (referenceSize) {
                 	case JvmUtil.ADDRESSING_4_BYTE:   
                 		unsafe.putInt(rootObj, fieldOffset, (int) JvmUtil.NULL);
                 		break;
                 	case JvmUtil.ADDRESSING_8_BYTE:
                 		unsafe.putLong(rootObj, fieldOffset, JvmUtil.NULL);
                 		break;	
                 	default:    
                        throw new AssertionError("Unsupported reference size: " + referenceSize);
            	}
	    	}
	    	else {
//	    		synchronized (fieldObj) {
	    			long fieldObjAddress = JvmUtil.toJvmAddress(addressOf(fieldObj));
			    	switch (JvmUtil.getAddressSize()) {
				    	case JvmUtil.SIZE_32_BIT:
				    		putAsIntAddress(rootObj, fieldOffset, fieldObjAddress);
				            break;
				        case JvmUtil.SIZE_64_BIT:
				        	int referenceSize = JvmUtil.getReferenceSize();
				        	switch (referenceSize) {
				        		case JvmUtil.ADDRESSING_4_BYTE:   
				        			putAsIntAddress(rootObj, fieldOffset, fieldObjAddress);
				        			break;
				                case JvmUtil.ADDRESSING_8_BYTE:
				                 	unsafe.putLong(rootObj, fieldOffset, fieldObjAddress);
				                 	break;
				                 default:    
				                    throw new AssertionError("Unsupported reference size: " + referenceSize);
				        	}
				            break; 
				         default:
				            throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());     
			    	}
//	    		}
	    	}	
//		}
    }
    
    @Override
    public Object getArrayElement(Object array, int elementIndex) {
//    	synchronized (array) {
//    		long arrayElementAddress = JvmUtil.getArrayElementAddress(array, elementIndex);
//    		long arrayElementPointedAddress = JvmUtil.toNativeAddress(unsafe.getAddress(arrayElementAddress));
//        	if (arrayElementPointedAddress != JvmUtil.NULL && arrayElementPointedAddress != JvmUtil.INVALID_ADDRESS) {
//        		return getObject(arrayElementPointedAddress);
//        	}
//        	else {
//        		return null;
//        	}
//		} 
    	Class<?> elementType = array.getClass().getComponentType();
		long elementOffset = JvmUtil.arrayBaseOffset(elementType) + 
				(elementIndex * JvmUtil.arrayIndexScale(elementType));
		return unsafe.getObject(array, elementOffset);
    }
    
    @Override
    public void setArrayElement(Object array, int elementIndex, Object element) {
//    	synchronized (array) {
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
//    	}	
    	/*
//    	synchronized (array) {
    		Class<?> elementType = array.getClass().getComponentType();
    		long elementOffset = JvmUtil.arrayBaseOffset(elementType) + 
    				(elementIndex * JvmUtil.arrayIndexScale(elementType));
	    	long elementAddress = JvmUtil.toJvmAddress(addressOf(element));
	    	if (elementAddress != JvmUtil.INVALID_ADDRESS) {
	    		switch (JvmUtil.getAddressSize()) {
		            case JvmUtil.SIZE_32_BIT:
		            	putAsIntAddress(array, elementOffset, elementAddress);
		                break;
		            case JvmUtil.SIZE_64_BIT:
		            	int referenceSize = JvmUtil.getReferenceSize();
		            	switch (referenceSize) {
		                 	case JvmUtil.ADDRESSING_4_BYTE:   
		                 		putAsIntAddress(array, elementOffset, elementAddress);
		                 		break;
		                 	case JvmUtil.ADDRESSING_8_BYTE:
		                 		unsafe.putLong(array, elementOffset, elementAddress);
		                 		break;
		            	}
		            	break; 
	    		}      
	    	}
//    	}	
    	*/
    }
    
    @Override
    public <T> T getObject(long address) {
    	if (unsafe.getAddress(address) == JvmUtil.NULL) {
    		return null;
    	}
    	address = JvmUtil.toJvmAddress(address);
    	Object[] objArray = objArrayBuffers.get();
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
    	return (T) unsafe.getAndSetObject(objArray, JvmUtil.getBaseOffset(), null); 
    }
    
	@Override
    public <T> void setObject(long address, T obj) {
		long objAddress = addressOf(obj);
		switch (JvmUtil.getAddressSize()) {
	        case JvmUtil.SIZE_32_BIT:
	            putAsIntAddress(address, objAddress);
	            break;
	        case JvmUtil.SIZE_64_BIT:
	        	int referenceSize = JvmUtil.getReferenceSize();
	        	switch (referenceSize) {
	             	case JvmUtil.ADDRESSING_4_BYTE:
	             		putAsIntAddress(address, objAddress);
	             		break;
	             	case JvmUtil.ADDRESSING_8_BYTE:
	             		unsafe.putLong(address, objAddress);
	             		break;
	        	}
	            break;
	    } 
    }
    
    @Override
    public <T> T changeObject(T source, T target) {
        if (source == null) {
            throw new IllegalArgumentException("Source object is null !");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target object is null !");
        }
//        synchronized (source) {
//        	synchronized (target) {
                long targetAddress = addressOf(target);
                setObject(targetAddress, source);
                return target;
//    		}
//        }
    }
    
    @Override
    public <T> T copyObject(T original) {
    	if (original == null) {
            throw new IllegalArgumentException("Original object is null !");
        }
    	Object[] objArray = objArrayBuffers.get();
//    	synchronized (original) {
    		long originalAddress = addressOf(original);
            switch (JvmUtil.getAddressSize()) {
    	        case JvmUtil.SIZE_32_BIT:
    	        	putAsIntAddress(objArray, JvmUtil.getBaseOffset(), originalAddress);
    	            break;
    	        case JvmUtil.SIZE_64_BIT:
    	        	int referenceSize = JvmUtil.getReferenceSize();
                	switch (referenceSize) {
                     	case JvmUtil.ADDRESSING_4_BYTE:
                     		putAsIntAddress(objArray, JvmUtil.getBaseOffset(), originalAddress);
                     		break;
                     	case JvmUtil.ADDRESSING_8_BYTE:
                     		unsafe.putLong(objArray, JvmUtil.getBaseOffset(), originalAddress);
                     		break;
                	}
    	            break;  
            }
            return (T) unsafe.getAndSetObject(objArray, JvmUtil.getBaseOffset(), null); 
//		} 
    }
    
    @Override
    public boolean getBoolean(long address) {
    	return unsafe.getByte(address) == 0x00 ? false : true;
    }

    @Override
    public void putBoolean(long address, boolean x) {
    	unsafe.putByte(address, x == true ? (byte) 0x01 : (byte) 0x00);
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
    public boolean getBooleanVolatile(long address) {
    	return unsafe.getByteVolatile(null, address) == 0x00 ? false : true;
    }

    @Override
    public void putBooleanVolatile(long address, boolean x) {
    	unsafe.putByteVolatile(null, address, x == true ? (byte) 0x01 : (byte) 0x00);
    }
    
    @Override
    public byte getByteVolatile(long address) {
    	return unsafe.getByteVolatile(null, address);
    }
    
    @Override
    public void putByteVolatile(long address, byte x) {
    	unsafe.putByteVolatile(null, address, x);
    }
    
    @Override
    public char getCharVolatile(long address) {
    	return unsafe.getCharVolatile(null, address);
    }

    @Override
    public void putCharVolatile(long address, char x) {
    	unsafe.putCharVolatile(null, address, x);
    }

    @Override
    public short getShortVolatile(long address) {
    	return unsafe.getShortVolatile(null, address);
    }

    @Override
    public void putShortVolatile(long address, short x) {
    	unsafe.putShortVolatile(null, address, x);
    }
   
    @Override
    public int getIntVolatile(long address) {
    	return unsafe.getIntVolatile(null, address);
    }

    @Override
    public void putIntVolatile(long address, int x) {
    	unsafe.putIntVolatile(null, address, x);
    }
    
    @Override
    public float getFloatVolatile(long address) {
    	return unsafe.getFloatVolatile(null, address);
    }

    @Override
    public void putFloatVolatile(long address, float x) {
    	unsafe.putFloatVolatile(null, address, x);
    }

    @Override
    public long getLongVolatile(long address) {
    	return unsafe.getLongVolatile(null, address);
    }

    @Override
    public void putLongVolatile(long address, long x) {
    	unsafe.putLongVolatile(null, address, x);
    }

    @Override
    public double getDoubleVolatile(long address) {
    	return unsafe.getDoubleVolatile(null, address);
    }

    @Override
    public void putDoubleVolatile(long address, double x) {
    	unsafe.putDoubleVolatile(null, address, x);
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
    public long getAddress(Object o, long offset) {
    	switch (JvmUtil.getAddressSize()) {
	        case JvmUtil.SIZE_32_BIT:
	        	return getAsIntAddress(o, offset);
	        case JvmUtil.SIZE_64_BIT:
	        	return getLong(o, offset);
	        default:
	        	throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());
    	}  	
    }
    
    @Override
    public void putAddress(Object o, long offset, long x) {
    	switch (JvmUtil.getAddressSize()) {
	        case JvmUtil.SIZE_32_BIT:
	        	putAsIntAddress(o, offset, x);
	        case JvmUtil.SIZE_64_BIT:
	        	putLong(o, offset, x);
	        default:
	        	throw new AssertionError("Unsupported address size: " + JvmUtil.getAddressSize());
		}  	
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
    public boolean getBooleanVolatile(Object o, long offset) {
    	return unsafe.getBooleanVolatile(o, offset);
    }
    
    @Override
    public void putBooleanVolatile(Object o, long offset, boolean x) {
    	unsafe.putBooleanVolatile(o, offset, x);
    }
    
    @Override
    public byte getByteVolatile(Object o, long offset) {
    	return unsafe.getByteVolatile(o, offset);
    }
   
    @Override
    public void putByteVolatile(Object o, long offset, byte x) {
    	unsafe.putByteVolatile(o, offset, x);
    }
    
    @Override
    public char getCharVolatile(Object o, long offset) {
    	return unsafe.getCharVolatile(o, offset);
    }
    
    @Override
    public void putCharVolatile(Object o, long offset, char x) {
    	unsafe.putCharVolatile(o, offset, x);
    }
   
    @Override
    public short getShortVolatile(Object o, long offset) {
    	return unsafe.getShortVolatile(o, offset);
    }
    
    @Override
    public void putShortVolatile(Object o, long offset, short x) {
    	unsafe.putShortVolatile(o, offset, x);
    }
    
    @Override
    public int getIntVolatile(Object o, long offset) {
    	return unsafe.getIntVolatile(o, offset);
    }

    @Override
    public void putIntVolatile(Object o, long offset, int x) {
    	unsafe.putIntVolatile(o, offset, x);
    }

    @Override
    public float getFloatVolatile(Object o, long offset) {
    	return unsafe.getFloatVolatile(o, offset);
    }
    
    @Override
    public void putFloatVolatile(Object o, long offset, float x) {
    	unsafe.putFloatVolatile(o, offset, x);
    }
    
    @Override
    public long getLongVolatile(Object o, long offset) {
    	return unsafe.getLongVolatile(o, offset);
    }
    
    @Override
    public void putLongVolatile(Object o, long offset, long x) {
    	unsafe.putLongVolatile(o, offset, x);
    }
   
    @Override
    public double getDoubleVolatile(Object o, long offset) {
    	return unsafe.getDoubleVolatile(o, offset);
    }
    
    @Override
    public void putDoubleVolatile(Object o, long offset, double x) {
    	unsafe.putDoubleVolatile(o, offset, x);
    }
    
    @Override
    public Object getObjectVolatile(Object o, long offset) {
    	return unsafe.getObjectVolatile(o, offset);
    }

    @Override
    public void putObjectVolatile(Object o, long offset, Object x) {
    	unsafe.putObjectVolatile(o, offset, x);
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
 		unsafe.putLong(offset, intAddress);
 		unsafe.putLong(obj, offset + JvmUtil.INT_SIZE, l);
    }

}
