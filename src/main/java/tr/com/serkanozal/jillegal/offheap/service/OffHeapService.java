/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.service;

import tr.com.serkanozal.jillegal.offheap.domain.model.instance.InstanceRequest;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.pool.ObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.factory.OffHeapPoolFactory;

public interface OffHeapService {

	boolean isEnable();

	DirectMemoryService getDirectMemoryService();
	void setDirectMemoryService(DirectMemoryService directMemoryService);
	
	OffHeapPoolFactory getDefaultOffHeapPoolFactory();
	void setOffHeapPoolFactory(OffHeapPoolFactory offHeapPoolFactory);
	
	<P extends OffHeapPoolCreateParameter<?>> OffHeapPoolFactory getOffHeapPoolFactory(Class<P> clazz);
	<P extends OffHeapPoolCreateParameter<?>> void setOffHeapPoolFactory(Class<P> clazz, OffHeapPoolFactory offHeapPoolFactory);
	
	<T, P extends OffHeapPoolCreateParameter<T>> ObjectOffHeapPool<T, P> getObjectOffHeapPool(Class<T> clazz);
	<T, P extends OffHeapPoolCreateParameter<T>> void setObjectOffHeapPool(Class<T> clazz, ObjectOffHeapPool<T, P> objectOffHeapPool);
	
	<T, O extends OffHeapPool<T, ?>> O createOffHeapPool(OffHeapPoolCreateParameter<T> parameter);
	
	<T> void makeOffHeapable(Class<T> elementType);
	
	<T> T newInstance(InstanceRequest<T> request);
	<T> long newInstanceAsAddress(InstanceRequest<T> request);
	<T> boolean freeInstance(T instance);
	boolean freeInstanceWithAddress(long address);
	<T> boolean isFreeInstance(T instance);
	boolean isFreeInstanceWithAddress(long address);
	
	<T> T newObject(Class<T> objectType);
	<T> long newObjectAsAddress(Class<T> objectType);
	<T> boolean freeObject(T obj);
	boolean freeObjectWithAddress(long address);
	
	<A> A newArray(Class<A> arrayType, int length);
	<A> long newArrayAsAddress(Class<A> arrayType, int length);
	<A> boolean freeArray(A array);
	boolean freeArrayWithAddress(long address);
	
	String newString(String str);
	String newString(char[] chars);
	String newString(char[] chars, int offset, int length);
	long newStringAsAddress(String str);
	boolean freeString(String str);
	boolean freeStringWithAddress(long address);
	
	Boolean getOffHeapBoolean(boolean b);
	Byte getOffHeapByte(byte b);
	Character getOffHeapCharacter(char c);
	Short getOffHeapShort(short s);
	Integer getOffHeapInteger(int i);
	Float getOffHeapFloat(float f);
	Long getOffHeapLong(long l);
	Double getOffHeapDouble(double d);
	
	<T> boolean isInOffHeap(T obj);
	
}
