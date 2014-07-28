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

import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapServiceFactory;

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
public class OffHeapJudyHashMap<K, V> extends AbstractMap<K, V> {

	private final static int BITS_IN_BYTE = 8;
	private final static int MAX_LEVEL = (Integer.SIZE / BITS_IN_BYTE) - 1; 
	private final static int NODE_SIZE = 256;
	
	private static final OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	private static final DirectMemoryService directMemoryService = DirectMemoryServiceFactory.getDirectMemoryService();
	
	private JudyTree<K, V> root = new JudyTree<K, V>();
	
	public OffHeapJudyHashMap() {

	}
	
	@Override
	public int size() {
		return root.size;
	}
	
	@Override
	public boolean isEmpty() {
		return root.size == 0;
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
	public V put(K key, V value) {
		return (V) root.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		return (V) root.get((K) key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key) {
		return (V) root.remove((K) key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key) {
		return root.containsKey((K) key);
	}
	
	@Override
	public void clear() {
		root.clear();
	}
	
	class JudyEntrySet extends AbstractSet<Map.Entry<K, V>> {

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new JudyEntryIterator(root.firstEntry);
		}

		@Override
		public int size() {
			return root.size;
		}
		
	}
	
	class JudyEntryIterator implements Iterator<Map.Entry<K, V>> {

		JudyEntry<K, V> currentEntry;
		
		JudyEntryIterator(JudyEntry<K, V> firstEntry) {
			currentEntry = firstEntry;
		}
		
		@Override
		public boolean hasNext() {
			if (currentEntry == null) {
				return false;
			}
			else {
				return currentEntry.next != null;
			}
		}

		@Override
		public JudyEntry<K, V> next() {
			if (currentEntry != null) {
				JudyEntry<K, V> nextEntry = currentEntry;
				currentEntry = currentEntry.next;
				return nextEntry;
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
			return new JudyKeyIterator(new JudyEntryIterator(root.firstEntry));
		}

		@Override
		public int size() {
			return root.size;
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
				return entry.key;
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
			return new JudyValueIterator(new JudyEntryIterator(root.firstEntry));
		}

		@Override
		public int size() {
			return root.size;
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
				return entry.value;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("\"remove()\" operation is not supported by JudyEntryIterator !");
		}
		
	}
	
	static class JudyEntry<K, V> implements Map.Entry<K, V> {

		K key;
		V value;
		JudyEntry<K, V> prev;
		JudyEntry<K, V> next;
		
		JudyEntry(K key) {
			this.key = key;
		}
		
		JudyEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public K getKey() {
			return key;
		}
		
		void setKey(K key) {
			directMemoryService.setObjectField(this, "key", key);
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			directMemoryService.setObjectField(this, "value", value);
			return value;
		}
		
		JudyEntry<K, V> getPrev() {
			return prev;
		}
		
		void setPrev(JudyEntry<K, V> prev) {
			directMemoryService.setObjectField(this, "prev", prev);
		}
		
		JudyEntry<K, V> getNext() {
			return next;
		}
		
		void setNext(JudyEntry<K, V> next) {
			directMemoryService.setObjectField(this, "next", next);
		}
		
	}
	
	static abstract class JudyNode<K, V> {
		
		JudyTree<K, V> root;
		
		JudyTree<K, V> getRoot() {
			return root;
		}
		
		void setRoot(JudyTree<K, V> root) {
			directMemoryService.setObjectField(this, "root", root);
		}
		
		abstract V get(int hash, byte level);
		abstract V put(int hash, K key, V value, byte level);
		abstract V remove(int hash, byte level);
		abstract boolean containsKey(int hash, byte level);
		abstract void clear(byte level);
		
	}
	
	static class JudyIntermediateNode<K, V> extends JudyNode<K, V> {
		
		JudyNode<K, V>[] children;
		
		JudyIntermediateNode() {
			init();
		}
		
		@SuppressWarnings("unchecked")
		void init() {
			setChildren(offHeapService.newArray(JudyNode[].class, NODE_SIZE)); // children = new OffHeapJudyHashMap.JudyNode[NODE_SIZE];
		}
		
		JudyNode<K, V>[] getChildren() {
			return children;
		}
		
		void setChildren(JudyNode<K, V>[] children) {
			directMemoryService.setObjectField(this, "children", children);
		}
		
		void initIfNeeded() {
			if (children == null) {
				init();
			}
		}

		@Override
		V get(int hash, byte level) {
			initIfNeeded();
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
		V put(int hash, K key, V value, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode<K, V> child = children[index];
			if (child == null) {
				if (nextLevel < MAX_LEVEL) {
					child = offHeapService.newObject(JudyIntermediateNode.class); // new JudyIntermediateNode();
					child.setRoot(root);
				}
				else {
					child = offHeapService.newObject(JudyLeafNode.class); // new JudyLeafNode();
					child.setRoot(root); 
				}
				directMemoryService.setArrayElement(children, index, child); // children[index] = child;
			}
			return child.put(hash, key, value, nextLevel);
		}
		
		@Override
		V remove(int hash, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode<K, V> child = children[index];
			if (child != null) {
				return child.remove(hash, nextLevel);
			}
			return null;
		}
		
		@Override
		boolean containsKey(int hash, byte level) {
			initIfNeeded();
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
						//directMemoryService.freeObject(child);
					}	
					// child = null; // Now it can be collected by GC
				}
			}
			directMemoryService.freeObject(children);
			setChildren(null); // children = null; // Now it can be collected by GC
		}
		
	}
	
	static class JudyLeafNode<K, V> extends JudyNode<K, V> {
		
		JudyEntry<K, V>[] entries;
		
		JudyLeafNode() {
			init();
		}
		
		@SuppressWarnings("unchecked")
		void init() {
			setEntries(offHeapService.newArray(JudyEntry[].class, NODE_SIZE)); // entries = new JudyEntry[NODE_SIZE];
		}
		
		JudyEntry<K, V>[] getEntries() {
			return entries;
		}
		
		void setEntries(JudyEntry<K, V>[] entries) {
			directMemoryService.setObjectField(this, "entries", entries);
		}
		
		void initIfNeeded() {
			if (entries == null) {
				init();
			}
		}

		@Override
		V get(int hash, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry<K, V> entry = entries[index];
			if (entry != null) {
				return entry.value;
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		V put(int hash, K key, V value, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry<K, V> entry = entries[index];
			if (entry == null) {
				entry = offHeapService.newObject(JudyEntry.class); // new JudyEntry(key);
				entry.setKey(key); 
				directMemoryService.setArrayElement(entries, index, entry); // entries[index] = entry;
				synchronized (root) {
					if (root.firstEntry == null) {
						root.setFirstEntry(entry);
					}
					if (root.lastEntry != null) {
						entry.setPrev(root.lastEntry);
						root.lastEntry.setNext(entry);
					}
					root.setLastEntry(entry);
				}	
			}
			entry.setValue(value);
			return value;
		}
		
		@Override
		V remove(int hash, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry<K, V> entryToRemove = entries[index];
			if (entryToRemove != null) {
				directMemoryService.setArrayElement(entries, index, null); // entries[index] = null;
				synchronized (root) {
					if (entryToRemove == root.firstEntry) {
						root.setFirstEntry(entryToRemove.next);
					}
					if (entryToRemove == root.lastEntry) {
						root.setLastEntry(entryToRemove.prev);
					}
				}
				if (entryToRemove.prev != null) {
					synchronized (entryToRemove.prev) {
						entryToRemove.prev.setNext(entryToRemove.next);
					}
				}
				if (entryToRemove.next != null) {
					synchronized (entryToRemove.next) {
						entryToRemove.next.setPrev(entryToRemove.prev);
					}	
				}
				return entryToRemove.value;
			}
			return null;
		}
		
		@Override
		boolean containsKey(int hash, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			return entries[index] != null;
		}
		
		@Override
		void clear(byte level) {
			directMemoryService.freeObject(entries);
			setEntries(null); // entries = null; // Now it can be collected by GC
		}
		
	}
	
	/**
	 * Root node for Judy tree based indexing nodes
	 */
	static class JudyTree<K, V> {

		JudyIntermediateNode<K, V>[] nodes;
		JudyEntry<K, V> firstEntry;
		JudyEntry<K, V> lastEntry;
		volatile int size;
		
		@SuppressWarnings("unchecked")
		JudyTree() {
			// Create and initialize first level nodes
			setNodes(offHeapService.newArray(JudyIntermediateNode[].class, NODE_SIZE));
			for (int i = 0 ; i < nodes.length; i++) {
				nodes[i].setRoot(this);
			}
			/*
			nodes = new OffHeapJudyHashMap.JudyIntermediateNode[NODE_SIZE];
			for (int i = 0 ; i < nodes.length; i++) {
				nodes[i] = new JudyIntermediateNode();
			}
			*/
		}
		
		JudyIntermediateNode<K, V>[] getNodes() {
			return nodes;
		}
		
		void setNodes(JudyIntermediateNode<K, V>[] nodes) {
			directMemoryService.setObjectField(this, "nodes", nodes);
		}
		
		JudyEntry<K, V> getFirstEntry() {
			return firstEntry;
		}
		
		void setFirstEntry(JudyEntry<K, V> firstEntry) {
			directMemoryService.setObjectField(this, "firstEntry", firstEntry);
		}
		
		JudyEntry<K, V> getLastEntry() {
			return lastEntry;
		}
		
		void setLastEntry(JudyEntry<K, V> lastEntry) {
			directMemoryService.setObjectField(this, "lastEntry", lastEntry);
		}
		
		V get(K key) {
			// Use most significant byte as first level index
			short index = (short)((key.hashCode() >> 24) & 0x000000FF);
			return nodes[index].get(key.hashCode(), (byte) 1);
		}
		
		V put(K key, V value) {
			// Use most significant byte as first level index
			short index = (short)((key.hashCode() >> 24) & 0x000000FF);
			V obj = nodes[index].put(key.hashCode(), key, value, (byte) 1);
			if (obj != null) {
				size++;
			}
			return obj;
		}
		
		V remove(K key) {
			// Use most significant byte as first level index
			short index = (short)((key.hashCode() >> 24) & 0x000000FF);
			V obj = nodes[index].remove(key.hashCode(), (byte) 1);
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
