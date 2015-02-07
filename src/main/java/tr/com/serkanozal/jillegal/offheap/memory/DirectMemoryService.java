/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.memory;

import tr.com.serkanozal.jillegal.offheap.memory.allocator.MemoryAllocator;

public interface DirectMemoryService {

	MemoryAllocator getMemoryAllocator();
	void setMemoryAllocator(MemoryAllocator memoryAllocator);
	
	long allocateMemory(long size);
	void freeMemory(long address);
	<T> void freeObject(T obj);
	Object allocateInstance(Class<?> clazz);
	void copyMemory(long sourceAddress, long destinationAddress, long size);
	void copyMemory(Object sourceObject, long sourceOffset, Object destinationObject, 
    		long destinationOffset, long size);
	void setMemory(long sourceAddress, long bytes, byte val);
    
	<T> long sizeOfObject(T obj);
	long sizeOfClass(Class<?> objClass);
	
	long addressOf(Object obj);
    long addressOfField(Object obj, String fieldName) throws SecurityException, NoSuchFieldException;
    long addressOfClass(Class<?> clazz);
    
    <O, F> F getObjectField(O obj, int fieldOffset);
    <O, F> void setObjectField(O rootObj, int fieldOffset, F fieldObj);
    <O, F> F getObjectField(O obj, String fieldName);
    <O, F> void setObjectField(O rootObj, String fieldName, F fieldObj);
    
    Object getArrayElement(Object array, int elementIndex);
    void setArrayElement(Object array, int elementIndex, Object element);
    
    <T> T getObject(long address);
    <T> void setObject(long address, T obj);
	<T> T changeObject(T source, T target);
	<T> T copyObject(T original);
	
	boolean getBoolean(long address);
	void putBoolean(long address, boolean x);
	byte getByte(long address);
	void putByte(long address, byte x);
	char getChar(long address);
	void putChar(long address, char x);
	short getShort(long address);
	void putShort(long address, short x);
	int getInt(long address);
	void putInt(long address, int x);
	float getFloat(long address);
	void putFloat(long address, float x);
	long getLong(long address);
	void putLong(long address, long x);
	double getDouble(long address);
	void putDouble(long address, double x);
	long getAddress(long address);
	void putAddress(long address, long x);
	long getAddress(Object o, long offset);
	void putAddress(Object o, long offset, long x);
	
	boolean getBooleanVolatile(long address);
	void putBooleanVolatile(long address, boolean x);
	byte getByteVolatile(long address);
	void putByteVolatile(long address, byte x);
	char getCharVolatile(long address);
	void putCharVolatile(long address, char x);
	short getShortVolatile(long address);
	void putShortVolatile(long address, short x);
	int getIntVolatile(long address);
	void putIntVolatile(long address, int x);
	float getFloatVolatile(long address);
	void putFloatVolatile(long address, float x);
	long getLongVolatile(long address);
	void putLongVolatile(long address, long x);
	double getDoubleVolatile(long address);
	void putDoubleVolatile(long address, double x);
	
	boolean getBoolean(Object o, long offset);
	void putBoolean(Object o, long offset, boolean x);
	byte getByte(Object o, long offset);
	void putByte(Object o, long offset, byte x);
	char getChar(Object o, long offset);
	void putChar(Object o, long offset, char x);
	short getShort(Object o, long offset);
	void putShort(Object o, long offset, short x);
	int getInt(Object o, long offset);
	void putInt(Object o, long offset, int x);
	float getFloat(Object o, long offset);
	void putFloat(Object o, long offset, float x);
	long getLong(Object o, long offset);
	void putLong(Object o, long offset, long x);
	double getDouble(Object o, long offset);
	void putDouble(Object o, long offset, double x);
	Object getObject(Object o, long offset);
	void putObject(Object o, long offset, Object x);
	
	boolean getBooleanVolatile(Object o, long offset);
	void putBooleanVolatile(Object o, long offset, boolean x);
	byte getByteVolatile(Object o, long offset);
	void putByteVolatile(Object o, long offset, byte x);
	char getCharVolatile(Object o, long offset);
	void putCharVolatile(Object o, long offset, char x);
	short getShortVolatile(Object o, long offset);
	void putShortVolatile(Object o, long offset, short x);
	int getIntVolatile(Object o, long offset);
	void putIntVolatile(Object o, long offset, int x);
	float getFloatVolatile(Object o, long offset);
	void putFloatVolatile(Object o, long offset, float x);
	long getLongVolatile(Object o, long offset);
	void putLongVolatile(Object o, long offset, long x);
	double getDoubleVolatile(Object o, long offset);
	void putDoubleVolatile(Object o, long offset, double x);
	Object getObjectVolatile(Object o, long offset);
	void putObjectVolatile(Object o, long offset, Object x);
	
	long getAsIntAddress(long address);
	long getAsIntAddress(Object obj, long offset);
	void putAsIntAddress(long address, long intAddress);
	void putAsIntAddress(Object obj, long offset, long intAddress);

}
