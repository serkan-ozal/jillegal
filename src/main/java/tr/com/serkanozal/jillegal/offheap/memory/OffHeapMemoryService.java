/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.memory;

public interface OffHeapMemoryService {

	long allocateMemory(long size);
	void freeMemory(long address);
	Object allocateInstance(Class<?> clazz);
	void copyMemory(long sourceAddress, long destinationAddress, long size);
	
	long sizeOf(Class<?> objClass);
	
	long internalAddressOf(Object obj);
	long addressOf(Object obj);
    long addressOfField(Object obj, String fieldName) throws SecurityException, NoSuchFieldException;

    <T> T getObject(long address);
    <T> void setObject(long address, T obj);
	<T> T changeObject(T source, T target);

}
