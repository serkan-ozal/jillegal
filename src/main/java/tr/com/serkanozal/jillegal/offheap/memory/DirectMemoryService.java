/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.memory;

public interface DirectMemoryService {

	long allocateMemory(long size);
	void freeMemory(long address);
	Object allocateInstance(Class<?> clazz);
	void copyMemory(long sourceAddress, long destinationAddress, long size);
	
	long sizeOf(Class<?> objClass);
	
	long addressOf(Object obj);
    long addressOfField(Object obj, String fieldName) throws SecurityException, NoSuchFieldException;
    long addressOfClass(Class<?> clazz);
    
    <T> T getObject(long address);
    <T> void setObject(long address, T obj);
	<T> T changeObject(T source, T target);
	<T> T copyObject(T original);
	
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

}
