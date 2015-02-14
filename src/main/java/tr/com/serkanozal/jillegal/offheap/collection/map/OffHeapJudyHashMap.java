/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.collection.map;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import tr.com.serkanozal.jillegal.offheap.config.provider.annotation.OffHeapIgnoreInstrumentation;
import tr.com.serkanozal.jillegal.offheap.domain.builder.pool.ObjectOffHeapPoolCreateParameterBuilder;
import tr.com.serkanozal.jillegal.offheap.domain.model.pool.ObjectPoolReferenceType;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.pool.impl.LazyReferencedObjectOffHeapPool;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;
import tr.com.serkanozal.jillegal.util.JvmUtil;

/**
 * Holds and indexes entities by Judy tree based structure and gets at O(1) complexity.
 * Complexity doesn't depends on count of entities. 
 * Normally in CHM or HM,  complexity is O(1+n/k) where 
 * 		k is the number of buckets,
 * 		n is the number of entities.
 * In this map implementation, complexity is O(1) at every entity counts. But it uses more memory.
 * 
 * In Judy tree based indexing structure, there are 4 levels for 4 byte of hash code as integer.
 * Last level (level 4 or leaf node) is hold as values.
 */
@SuppressWarnings("restriction")
public class OffHeapJudyHashMap<K, V> extends AbstractMap<K, V> implements OffHeapMap<K, V> {

	private final static int BITS_IN_BYTE = 8;
	private final static int MAX_LEVEL = (Integer.SIZE / BITS_IN_BYTE) - 1; 
	private final static int NODE_SIZE = 256;
	
	private static final OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	private static final DirectMemoryService directMemoryService = DirectMemoryServiceFactory.getDirectMemoryService();
	
	private static final int KEY_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyEntry.class, "key"));
	private static final int VALUE_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyEntry.class, "value"));
	private static final int PREV_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyEntry.class, "prev"));
	private static final int NEXT_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyEntry.class, "next"));

	private static final int CHILDREN_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyIntermediateNode.class, "children"));
	
	private static final int ENTRIES_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyLeafNode.class, "entries"));
	
	private static final int NODES_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyTree.class, "nodes"));
	private static final int FIRST_ENTRY_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyTree.class, "firstEntry"));
	private static final int LAST_ENTRY_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyTree.class, "lastEntry"));
	
//	private static final int ROOT_FIELD_OFFSET = 
//			(int) JvmUtil.getUnsafe().objectFieldOffset(
//					JvmUtil.getField(OffHeapJudyHashMap.class, "root"));
	
//	private JudyTree<K, V> root;
	private long rootAddress;
	private Class<K> keyType;
	private Class<V> elementType;
	
	public OffHeapJudyHashMap() {
		this.rootAddress = createJudyTree();
//		directMemoryService.setObjectField(this, ROOT_FIELD_OFFSET, createJudyTree());
	}
	
	public OffHeapJudyHashMap(Class<V> elementType) {
		this.elementType = elementType;
		this.rootAddress = createJudyTree();
//		directMemoryService.setObjectField(this, ROOT_FIELD_OFFSET, createJudyTree());
	}
	
	public OffHeapJudyHashMap(Class<K> keyType, Class<V> elementType) {
		this.keyType = keyType;
		this.elementType = elementType;
		this.rootAddress = createJudyTree();
//		directMemoryService.setObjectField(this, ROOT_FIELD_OFFSET, createJudyTree());
	}
	
//	JudyTree<K, V> createJudyTree() {
	long createJudyTree() {
		LazyReferencedObjectOffHeapPool<JudyTree> objectPool = 
		offHeapService.createOffHeapPool(
				new ObjectOffHeapPoolCreateParameterBuilder<JudyTree>().
						type(JudyTree.class).
						objectCount(1).
						referenceType(ObjectPoolReferenceType.LAZY_REFERENCED).
					build());
		JudyTree<K, V> judyTree = objectPool.get(); // new JudyTree<K, V>();
		judyTree.init();
		return directMemoryService.addressOf(judyTree);
//		return judyTree;
	}
	
	JudyTree<K, V> getRoot() {
		return directMemoryService.getObject(rootAddress);
	}

	@Override
	public Class<V> getElementType() {
		return elementType;
	}
	
	@Override
	public V newElement() {
		if (elementType != null) {
			return offHeapService.newObject(elementType);
		}
		else {
			throw new IllegalStateException("Element type is not specified");
		}
	}
	
	@Override
	public K newKey() {
		if (keyType != null) {
			return offHeapService.newObject(keyType);
		}
		else {
			throw new IllegalStateException("Key type is not specified");
		}
	}
	
	@Override
	public int size() {
		return getRoot().size;
	}
	
	@Override
	public boolean isEmpty() {
		return getRoot().size == 0;
	}
    
	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("\"containsValue(Object value)\" operation is not supported right now !");
	}
    
    @Override
	public Set<K> keySet() {
    	return new JudyKeySet();
    }
    
    @Override
	public Collection<V> values() {
    	return new JudyValueCollection();
    }
    
    @Override
	public Set<Map.Entry<K, V>> entrySet() {
    	return new JudyEntrySet();
    }

	@Override
	public synchronized V put(K key, V value) {
		return (V) getRoot().put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized V get(Object key) {
		return (V) getRoot().get((K) key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized V remove(Object key) {
		return (V) getRoot().remove((K) key);	
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean containsKey(Object key) {
		return getRoot().containsKey((K) key);
	}
	
	@Override
	public synchronized void clear() {
		getRoot().clear();	
	}
	
	class JudyEntrySet extends AbstractSet<Map.Entry<K, V>> {

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new JudyEntryIterator(getRoot().firstEntry);
		}

		@Override
		public int size() {
			return getRoot().size;
		}
		
	}
	
	class JudyEntryIterator implements Iterator<Map.Entry<K, V>> {

		JudyEntry<K, V> currentEntry;
		
		JudyEntryIterator(JudyEntry<K, V> firstEntry) {
			currentEntry = firstEntry;
		}
		
		@Override
		public boolean hasNext() {
			return currentEntry != null;
		}

		@Override
		public JudyEntry<K, V> next() {
			if (currentEntry != null) {
				synchronized (OffHeapJudyHashMap.this) {
					JudyEntry<K, V> nextEntry = currentEntry;
					currentEntry = currentEntry.next;
					return nextEntry;
				}	
			}
			else {
				return null;
			}	
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("\"remove()\" operation is not supported by JudyEntryIterator !");
		}
		
	}
	
	class JudyKeySet extends AbstractSet<K> {

		@Override
		public Iterator<K> iterator() {
			return new JudyKeyIterator(new JudyEntryIterator(getRoot().firstEntry));
		}

		@Override
		public int size() {
			return getRoot().size;
		}
		
	}
	
	class JudyKeyIterator implements Iterator<K> {

		JudyEntryIterator entryIterator;
		
		JudyKeyIterator(JudyEntryIterator entryIterator) {
			this.entryIterator = entryIterator;
		}
		
		@Override
		public boolean hasNext() {
			return entryIterator.hasNext();
		}

		@Override
		public K next() {
			JudyEntry<K, V> entry = entryIterator.next();
			if (entry == null) {
				return null;
			}
			else {
				return entry.getKey();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("\"remove()\" operation is not supported by JudyEntryIterator !");
		}
		
	}
	
	class JudyValueCollection extends AbstractCollection<V> {

		@Override
		public Iterator<V> iterator() {
			return new JudyValueIterator(new JudyEntryIterator(getRoot().firstEntry));
		}

		@Override
		public int size() {
			return getRoot().size;
		}
		
	}
	
	class JudyValueIterator implements Iterator<V> {

		JudyEntryIterator entryIterator;
		
		JudyValueIterator(JudyEntryIterator entryIterator) {
			this.entryIterator = entryIterator;
		}
		
		@Override
		public boolean hasNext() {
			return entryIterator.hasNext();
		}

		@Override
		public V next() {
			JudyEntry<K, V> entry = entryIterator.next();
			if (entry == null) {
				return null;
			}
			else {
				return entry.getValue();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("\"remove()\" operation is not supported by JudyValueIterator !");
		}
		
	}
	
	@OffHeapIgnoreInstrumentation
	static class JudyEntry<K, V> implements Map.Entry<K, V> {

//		volatile long keyAddress;
//		volatile long valueAddress;
		K key;
		V value;
		JudyEntry<K, V> prev;
		JudyEntry<K, V> next;
		
		@Override
		public K getKey() {
//			if (keyAddress == JvmUtil.NULL) {
//				return null;
//			} else {
//				return directMemoryService.getObject(keyAddress);
//			}
			return key;
		}
		
		void setKey(K key) {
//			if (keyAddress != JvmUtil.NULL) {
//				offHeapService.freeObjectWithAddress(keyAddress);
//			}
//			if (key == null) {
//				keyAddress = JvmUtil.NULL;
//			} else {
//				keyAddress = directMemoryService.addressOf(key);
//			}
			K oldKey = getKey();
			if (oldKey != null) {
				directMemoryService.setObjectField(this, KEY_FIELD_OFFSET, null);
				offHeapService.freeObject(oldKey);
			}
			directMemoryService.setObjectField(this, KEY_FIELD_OFFSET, key);
		}

		@Override
		public V getValue() {
//			if (valueAddress == JvmUtil.NULL) {
//				return null;
//			} else {
//				return directMemoryService.getObject(valueAddress);
//			}
			return value;
		}

		@Override
		public V setValue(V value) {
//			V oldValue = null;
//			if (valueAddress != JvmUtil.NULL) {
//				oldValue = directMemoryService.getObject(valueAddress);
//			}
//			if (value == null) {
//				valueAddress = JvmUtil.NULL;
//			} else {
//				valueAddress = directMemoryService.addressOf(value);
//			}
			V oldValue = getValue();
			directMemoryService.setObjectField(this, VALUE_FIELD_OFFSET, value);
			return oldValue;
		}
		
		JudyEntry<K, V> getPrev() {
			return prev;
		}
		
		void setPrev(JudyEntry<K, V> prev) {
			directMemoryService.setObjectField(this, PREV_FIELD_OFFSET, prev);
		}
		
		JudyEntry<K, V> getNext() {
			return next;
		}
		
		void setNext(JudyEntry<K, V> next) {
			directMemoryService.setObjectField(this, NEXT_FIELD_OFFSET, next);
		}

	}
	
	@OffHeapIgnoreInstrumentation
	static abstract class JudyNode<K, V> {

		abstract void init();
		abstract V get(int hash, byte level);
		abstract V put(JudyTree<K, V> root, int hash, K key, V value, byte level);
		abstract V remove(JudyTree<K, V> root, int hash, byte level);
		abstract boolean containsKey(int hash, byte level);
		abstract void clear(byte level);
		
	}
	
	@OffHeapIgnoreInstrumentation
	static class JudyIntermediateNode<K, V> extends JudyNode<K, V> {

		JudyNode<K, V>[] children;

		@SuppressWarnings("unchecked")
		@Override
		void init() {
			setChildren(offHeapService.newArray(JudyNode[].class, NODE_SIZE, false));
		}
		
		JudyNode<K, V>[] getChildren() {
			return children;
		}
		
		void setChildren(JudyNode<K, V>[] children) {
			directMemoryService.setObjectField(this, CHILDREN_FIELD_OFFSET, children);
		}
		
		void initIfNeeded() {
			if (children == null) {
				init();
			}
		}

		@Override
		V get(int hash, byte level) {
			if (children == null) {
				return null;
			}
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode<K, V> child = children[index];
			if (child != null) {
				return child.get(hash, nextLevel);
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		V put(JudyTree<K, V> root, int hash, K key, V value, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode<K, V> child = children[index];
			if (child == null) {
				if (nextLevel < MAX_LEVEL) {
					child = offHeapService.newObject(JudyIntermediateNode.class);
				}
				else {
					child = offHeapService.newObject(JudyLeafNode.class); 
				}
				child.init();
				directMemoryService.setArrayElement(children, index, child);
			}
			return child.put(root, hash, key, value, nextLevel);
		}
		
		@Override
		V remove(JudyTree<K, V> root, int hash, byte level) {
			if (children == null) {
				return null;
			}
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode<K, V> child = children[index];
			if (child != null) {
				return child.remove(root, hash, nextLevel);
			}
			return null;
		}
		
		@Override
		boolean containsKey(int hash, byte level) {
			if (children == null) {
				return false;
			}
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode<K, V> child = children[index];
			if (child != null) {
				return child.containsKey(hash, nextLevel);
			}
			return false;
		}
		
		@Override
		void clear(byte level) {
			if (children != null) {
				// Clear child nodes
				for (JudyNode<K, V> child : children) {
					if (child != null) {
						child.clear((byte) (level + 1));
						offHeapService.freeObject(child);
					}	
				}
				offHeapService.freeArray(children);
				setChildren(null);
			}
		}
		
	}
	
	@OffHeapIgnoreInstrumentation
	static class JudyLeafNode<K, V> extends JudyNode<K, V> {

		JudyEntry<K, V>[] entries;

		@SuppressWarnings("unchecked")
		void init() {
			setEntries(offHeapService.newArray(JudyEntry[].class, NODE_SIZE, false));
		}
		
		JudyEntry<K, V>[] getEntries() {
			return entries;
		}
		
		void setEntries(JudyEntry<K, V>[] entries) {
			directMemoryService.setObjectField(this, ENTRIES_FIELD_OFFSET, entries);
		}
		
		void initIfNeeded() {
			if (entries == null) {
				init();
			}
		}

		@Override
		V get(int hash, byte level) {
			if (entries == null) {
				return null;
			}
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry<K, V> entry = entries[index];
			if (entry != null) {
				return entry.getValue();
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		V put(JudyTree<K, V> root, int hash, K key, V value, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry<K, V> entry = entries[index];
			if (entry == null) {
				entry = offHeapService.newObject(JudyEntry.class); 
				directMemoryService.setArrayElement(entries, index, entry);	
			}
			entry.setKey(key); 
			V oldValue = entry.setValue(value);
			
//			if (root.getFirstEntry() == null) {
//				root.setFirstEntry(entry);
//			}
//			JudyEntry<K, V> lastEntry = root.getLastEntry();
//			if (lastEntry != null) {
//				entry.setPrev(lastEntry);
//				lastEntry.setNext(entry);
//			}
//			root.setLastEntry(entry);
		
			return oldValue;
		}
		
		@Override
		V remove(JudyTree<K, V> root, int hash, byte level) {
			if (entries == null) {
				return null;
			}
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry<K, V> entryToRemove = entries[index];
			if (entryToRemove != null) {
				directMemoryService.setArrayElement(entries, index, null); 
				V removedValue = entryToRemove.getValue();
				entryToRemove.setKey(null); // key area will be free automatically in entry instance
				entryToRemove.setValue(null);

//				JudyEntry<K, V> prevEntry = entryToRemove.getPrev();
//				JudyEntry<K, V> nextEntry = entryToRemove.getNext();
//				entryToRemove.setPrev(null);
//				entryToRemove.setNext(null);
//				if (entryToRemove == root.getFirstEntry()) {
//					root.setFirstEntry(nextEntry);
//				}
//				if (entryToRemove == root.getLastEntry()) {
//					root.setLastEntry(prevEntry);
//				}
//				if (prevEntry != null) {
//					prevEntry.setNext(nextEntry);
//				}
//				if (nextEntry != null) {
//					nextEntry.setPrev(prevEntry);	
//				}
				
				offHeapService.freeObject(entryToRemove);
				return removedValue;
			}
			return null;
		}
		
		@Override
		boolean containsKey(int hash, byte level) {
			if (entries == null) {
				return false;
			}
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry<K, V> entry = entries[index];
			return entry != null;
		}
		
		@Override
		void clear(byte level) {
			for (int i = 0; i < entries.length; i++) {
				JudyEntry<K, V> entry = entries[i];
				if (entry == null) {
					continue;
				}
				entry.setKey(null); // key area will be free automatically in entry instance
				entry.setValue(null);
				entry.setPrev(null);
				entry.setNext(null);
				offHeapService.freeObject(entry);
			}
			offHeapService.freeArray(entries);
			setEntries(null);
		}
		
	}
	
	/**
	 * Root node for Judy tree based indexing nodes
	 */
	@OffHeapIgnoreInstrumentation
	static class JudyTree<K, V> {

		JudyIntermediateNode<K, V>[] nodes;
		JudyEntry<K, V> firstEntry;
		JudyEntry<K, V> lastEntry;
		volatile int size;

		@SuppressWarnings("unchecked")
		void init() {
			// Create and initialize first level nodes
			setNodes(offHeapService.newArray(JudyIntermediateNode[].class, NODE_SIZE, true));
		}
		
		JudyIntermediateNode<K, V>[] getNodes() {
			return nodes;
		}
		
		void setNodes(JudyIntermediateNode<K, V>[] nodes) {
			directMemoryService.setObjectField(this, NODES_FIELD_OFFSET, nodes);
		}
		
		JudyEntry<K, V> getFirstEntry() {
			return firstEntry;
		}
		
		void setFirstEntry(JudyEntry<K, V> firstEntry) {
			directMemoryService.setObjectField(this, FIRST_ENTRY_FIELD_OFFSET, firstEntry);
		}
		
		JudyEntry<K, V> getLastEntry() {
			return lastEntry;
		}
		
		void setLastEntry(JudyEntry<K, V> lastEntry) {
			directMemoryService.setObjectField(this, LAST_ENTRY_FIELD_OFFSET, lastEntry);
		}
		
		V get(K key) {
			// Use most significant byte as first level index
			short index = (short)((key.hashCode() >> 24) & 0x000000FF);
			return nodes[index].get(key.hashCode(), (byte) 1);
		}
		
		V put(K key, V value) {
			// Use most significant byte as first level index
			short index = (short)((key.hashCode() >> 24) & 0x000000FF);
			V obj = nodes[index].put(this, key.hashCode(), key, value, (byte) 1);
			if (obj == null) {
				size++;
			}
			return obj;
		}
		
		V remove(K key) {
			// Use most significant byte as first level index
			short index = (short)((key.hashCode() >> 24) & 0x000000FF);
			V obj = nodes[index].remove(this, key.hashCode(), (byte) 1);
			if (obj != null) {
				size--;
			}
			return obj;
		}
		
		boolean containsKey(K key) {
			// Use most significant byte as first level index
			short index = (short)((key.hashCode() >> 24) & 0x000000FF);
			return nodes[index].containsKey(key.hashCode(), (byte) 1);
		}
		
		void clear() {
			// Start clearing from first level child nodes
			for (int i = 0 ; i < nodes.length; i++) {
				nodes[i].clear((byte) 1);
			}
			size = 0;
		}
		
	}
	
}
