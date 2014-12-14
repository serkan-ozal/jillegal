/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.collection.map;

import java.util.Map;

import tr.com.serkanozal.jillegal.offheap.collection.OffHeapCollection;

public interface OffHeapMap<K, V> extends Map<K, V>, OffHeapCollection<V> {
	
	K newKey();
	
}
