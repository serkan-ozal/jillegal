/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.service;

import tr.com.serkanozal.jillegal.offheap.domain.model.pool.OffHeapPoolCreateParameter;
import tr.com.serkanozal.jillegal.offheap.pool.OffHeapPool;
import tr.com.serkanozal.jillegal.offheap.pool.factory.OffHeapPoolFactory;

/**
 	******************** IMPORTANT NOTE ********************
 	********************************************************
 
 	Since location of class may be changed by GC at "Compact" phase, 
	for disabling compacting, options below must be added as VM argument.
	
	https://blogs.oracle.com/jonthecollector/entry/presenting_the_permanent_generation
 */

/**
	Sun HotSpot JVM:
	
		Options:
			-XX:MarkSweepAlwaysCompactCount=999999999 
			-XX:HeapMaximumCompactionInterval=999999999
			-XX:HeapFirstMaximumCompactionCount=999999999
			-XX:-UseMaximumCompactionOnSystemGC
			-XX:-CMSCompactWhenClearAllSoftRefs
			-XX:-UseCMSCompactAtFullCollection
			-XX:CMSFullGCsBeforeCompaction=999999999
			
			-XX:MarkSweepAlwaysCompactCount=999999999 -XX:HeapMaximumCompactionInterval=999999999 -XX:HeapFirstMaximumCompactionCount=999999999 -XX:-UseMaximumCompactionOnSystemGC -XX:-CMSCompactWhenClearAllSoftRefs -XX:-UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=999999999
		
			Possibly "-XX:MarkSweepAlwaysCompactCount=999999999999999999" is enough
			
		References:	
			http://stas-blogspot.blogspot.com/2011/07/most-complete-list-of-xx-options-for.html
			http://jvm-options.tech.xebia.fr/

	********************************************************
	
	Oracle JRockit JVM:
		
		Options:
			-XXnocompaction or -XXcompaction:enable=false

		References:
			http://docs.oracle.com/cd/E13150_01/jrockit_jvm/jrockit/jrdocs/refman/optionXX.html
			http://docs.oracle.com/cd/E15289_01/doc.40/e15062/optionxx.htm#BABGFFID
			
	********************************************************
			
	IBM JVM (WebSphere or J9):
		
		Options:
			-Xnocompactgc
			-Xnoclassgc
			
		References:
			http://publib.boulder.ibm.com/infocenter/realtime/v1r0/index.jsp?topic=%2Fcom.ibm.rt.doc.10%2Frealtime%2Frt_xoptions_gc_standard.html
			http://publib.boulder.ibm.com/infocenter/javasdk/v1r4m2/index.jsp?topic=%2Fcom.ibm.java.doc.diagnostics.142j9%2Fhtml%2Fgcparameters.html
*/
public interface OffHeapService {

	OffHeapPoolFactory getDefaultOffHeapPoolFactory();
	void setOffHeapPoolFactory(OffHeapPoolFactory offHeapPoolFactory);
	
	<P extends OffHeapPoolCreateParameter<?>> OffHeapPoolFactory getOffHeapPoolFactory(Class<P> clazz);
	<P extends OffHeapPoolCreateParameter<?>> void setOffHeapPoolFactory(OffHeapPoolFactory offHeapPoolFactory, Class<P> clazz);
	
	<T, O extends OffHeapPool<T, ?>> O createOffHeapPool(OffHeapPoolCreateParameter<T> parameter);
	
	<T> void makeOffHeapable(Class<T> elementType);
	
	<T> T newObject(Class<T> objectType);
	<T> long newObjectAsAddress(Class<T> objectType);
	<T> void freeObject(T obj);
	void freeObjectWithAddress(long address);
	<T> boolean isFreeObject(T obj);
	boolean isFreeObjectWithAddress(long address);
	
	<A> A newArray(Class<A> arrayType, int length);
	<A> long newArrayAsAddress(Class<A> arrayType, int length);
	<A> void freeArray(A array);
	void freeArrayWithAddress(long address);
	
	String newString(String str);
	long newStringAsAddress(String str);
	void freeString(String str);
	void freeStringWithAddress(long address);
	
}
