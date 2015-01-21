/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.collection.set;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
 *
 * In this set implementation, complexity is O(1) at every entity counts. But it uses more memory.
 * 
 * In Judy tree based indexing structure, there are 4 levels for 4 byte of hash code as integer.
 * Last level (level 4 or leaf node) is hold as values.
 */
public class OffHeapJudyHashSet<E> extends AbstractSet<E> implements OffHeapSet<E> {

	private final static int BITS_IN_BYTE = 8;
	private final static int MAX_LEVEL = (Integer.SIZE / BITS_IN_BYTE) - 1; 
	private final static int NODE_SIZE = 256;
	
	private static final OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	private static final DirectMemoryService directMemoryService = DirectMemoryServiceFactory.getDirectMemoryService();
	
	@SuppressWarnings("restriction")
	private static final int PREV_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyEntry.class, "prev"));
	@SuppressWarnings("restriction")
	private static final int NEXT_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyEntry.class, "next"));
	
	@SuppressWarnings("restriction")
	private static final int ROOT_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyNode.class, "root"));
	
	@SuppressWarnings("restriction")
	private static final int CHILDREN_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyIntermediateNode.class, "children"));
	
	@SuppressWarnings("restriction")
	private static final int ENTRIES_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyLeafNode.class, "entries"));
	
	@SuppressWarnings("restriction")
	private static final int NODES_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyTree.class, "nodes"));
	@SuppressWarnings("restriction")
	private static final int FIRST_ENTRY_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyTree.class, "firstEntry"));
	@SuppressWarnings("restriction")
	private static final int LAST_ENTRY_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyTree.class, "lastEntry"));
	
	private JudyTree<E> root;
	private Class<E> elementType;
	private Lock judyTreeLock = new ReentrantLock();
	
	public OffHeapJudyHashSet() {
		this.root = createJudyTree();
	}
	
	public OffHeapJudyHashSet(Class<E> elementType) {
		this.elementType = elementType;
		this.root = createJudyTree();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	JudyTree<E> createJudyTree() {
		LazyReferencedObjectOffHeapPool<JudyTree> objectPool = 
				offHeapService.createOffHeapPool(
						new ObjectOffHeapPoolCreateParameterBuilder<JudyTree>().
								type(JudyTree.class).
								objectCount(1).
								referenceType(ObjectPoolReferenceType.LAZY_REFERENCED).
							build());
		JudyTree<E> judyTree = objectPool.get();
		judyTree.init();
		return judyTree;
	}
	
	@Override
	public Class<E> getElementType() {
		return elementType;
	}
	
	@Override
	public E newElement() {
		if (elementType != null) {
			return offHeapService.newObject(elementType);
		}
		else {
			throw new IllegalStateException("Element type is not specified");
		}
	}

	@Override
	public int size() {
		return root.size;
	}
	
	@Override
	public boolean isEmpty() {
		return root.size == 0;
	}
    
	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object element) {
		return root.contains((E) element);
	}
  
	@Override
	public boolean add(E element) {
		return root.add(element, judyTreeLock) == null;
	}
	
	@Override
	public E put(E element) {
		return root.add(element, judyTreeLock);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object element) {
		return root.remove((E) element, judyTreeLock) != null;
	}
	
	@Override
	public void clear() {
		root.clear();
	}
	
	@Override
	public Iterator<E> iterator() {
		return new JudyElementIterator(new JudyEntryIterator(root.firstEntry));
	}
	
	class JudyEntryIterator implements Iterator<JudyEntry<E>> {

		JudyEntry<E> currentEntry;
		
		JudyEntryIterator(JudyEntry<E> firstEntry) {
			currentEntry = firstEntry;
		}
		
		@Override
		public boolean hasNext() {
			return currentEntry != null;
		}
 
		@Override
		public JudyEntry<E> next() {
			if (currentEntry != null) {
				JudyEntry<E> nextEntry = currentEntry;
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
	
	class JudyElementCollection extends AbstractCollection<E> {

		@Override
		public Iterator<E> iterator() {
			return new JudyElementIterator(new JudyEntryIterator(root.firstEntry));
		}

		@Override
		public int size() {
			return root.size;
		}
		
	}
	
	class JudyElementIterator implements Iterator<E> {

		JudyEntryIterator entryIterator;
		
		JudyElementIterator(JudyEntryIterator entryIterator) {
			this.entryIterator = entryIterator;
		}
		
		@Override
		public boolean hasNext() {
			return entryIterator.hasNext();
		}

		@Override
		public E next() {
			JudyEntry<E> entry = entryIterator.next();
			if (entry == null) {
				return null;
			}
			else {
				return entry.getElement();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("\"remove()\" operation is not supported by JudyElementIterator !");
		}
		
	}
	
	static class JudyEntry<E> {

		long elementAddress = 0;
		JudyEntry<E> prev;
		JudyEntry<E> next;
		
		JudyEntry(E element) {
			this.elementAddress = directMemoryService.addressOf(element);
		}
		
		public E getElement() {
			if (elementAddress != 0) {
				return directMemoryService.getObject(elementAddress);
			} else {
				return null;
			}
		} 

		public E setElement(E element) {
			E oldElement = null;
			if (elementAddress != 0) {
				oldElement = directMemoryService.getObject(elementAddress);
			}
			if (element != null) {
				elementAddress = directMemoryService.addressOf(element);
			} 
			else {
				elementAddress = 0;
			}
			return oldElement;
		}
		
		JudyEntry<E> getPrev() {
			return prev;
		}
		
		void setPrev(JudyEntry<E> prev) {
			directMemoryService.setObjectField(this, PREV_FIELD_OFFSET, prev);
		}
		
		JudyEntry<E> getNext() {
			return next;
		}
		
		void setNext(JudyEntry<E> next) {
			directMemoryService.setObjectField(this, NEXT_FIELD_OFFSET, next);
		}
		
	}
	
	static abstract class JudyNode<E> {
		
		JudyTree<E> root;
		
		JudyTree<E> getRoot() {
			return root;
		}
		
		void setRoot(JudyTree<E> root) {
			directMemoryService.setObjectField(this, ROOT_FIELD_OFFSET, root);
		}
		
		abstract E add(int hash, E element, byte level, Lock judyTreeLock);
		abstract E remove(int hash, byte level, Lock judyTreeLock);
		abstract boolean contains(int hash, byte level);
		abstract void clear(byte level);
		
	}
	
	static class JudyIntermediateNode<E> extends JudyNode<E> {
		
		JudyNode<E>[] children;
		
		JudyIntermediateNode() {
			init();
		}
		
		@SuppressWarnings("unchecked")
		void init() {
			setChildren(offHeapService.newArray(JudyNode[].class, NODE_SIZE));
		}
		
		JudyNode<E>[] getChildren() {
			return children;
		}
		
		void setChildren(JudyNode<E>[] children) {
			directMemoryService.setObjectField(this, CHILDREN_FIELD_OFFSET, children);
		}
		
		void initIfNeeded() {
			if (children == null) {
				init();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		E add(int hash, E element, byte level, Lock judyTreeLock) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode<E> child = children[index];
			if (child == null) {
				if (nextLevel < MAX_LEVEL) {
					child = offHeapService.newObject(JudyIntermediateNode.class);
					child.setRoot(root);
				}
				else {
					child = offHeapService.newObject(JudyLeafNode.class);
					child.setRoot(root); 
				}
				directMemoryService.setArrayElement(children, index, child);
			}
			return child.add(hash, element, nextLevel, judyTreeLock);
		}
		
		@Override
		E remove(int hash, byte level, Lock judyTreeLock) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode<E> child = children[index];
			if (child != null) {
				return child.remove(hash, nextLevel, judyTreeLock);
			}
			return null;
		}
		
		@Override
		boolean contains(int hash, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode<E> child = children[index];
			if (child != null) {
				return child.contains(hash, nextLevel);
			}
			return false;
		}
		
		@Override
		void clear(byte level) {
			if (children != null) {
				// Clear child nodes
				for (JudyNode<E> child : children) {
					if (child != null) {
						child.clear((byte) (level + 1));
						offHeapService.freeObject(child);
					}	
				}
			}
			offHeapService.freeArray(children);
			setChildren(null);
		}
		
	}
	
	static class JudyLeafNode<E> extends JudyNode<E> {
		
		JudyEntry<E>[] entries;
		
		JudyLeafNode() {
			init();
		}
		
		@SuppressWarnings("unchecked")
		void init() {
			setEntries(offHeapService.newArray(JudyEntry[].class, NODE_SIZE));
		}
		
		JudyEntry<E>[] getEntries() {
			return entries;
		}
		
		void setEntries(JudyEntry<E>[] entries) {
			directMemoryService.setObjectField(this, ENTRIES_FIELD_OFFSET, entries);
		}
		
		void initIfNeeded() {
			if (entries == null) {
				init();
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		E add(int hash, E element, byte level, Lock judyTreeLock) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry<E> entry = entries[index];
			if (entry == null) {
				entry = offHeapService.newObject(JudyEntry.class); 
				directMemoryService.setArrayElement(entries, index, entry);
			}
			judyTreeLock.lock();
			try {
				if (root.firstEntry == null) {
					root.setFirstEntry(entry);
				}
				if (root.lastEntry != null) {
					entry.setPrev(root.lastEntry);
					root.lastEntry.setNext(entry);
				}
				root.setLastEntry(entry);
			}
			finally {
				judyTreeLock.unlock();
			}
			return entry.setElement(element);
		}
		
		@Override
		E remove(int hash, byte level, Lock judyTreeLock) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry<E> entryToRemove = entries[index];
			if (entryToRemove != null) {
				directMemoryService.setArrayElement(entries, index, null); 
				judyTreeLock.lock();
				try {
					if (entryToRemove == root.firstEntry) {
						root.setFirstEntry(entryToRemove.next);
					}
					if (entryToRemove == root.lastEntry) {
						root.setLastEntry(entryToRemove.prev);
					}
					if (entryToRemove.prev != null) {
						entryToRemove.prev.setNext(entryToRemove.next);
					}
					if (entryToRemove.next != null) {
						entryToRemove.next.setPrev(entryToRemove.prev);	
					}
				}
				finally {
					judyTreeLock.unlock();
				}
				return entryToRemove.getElement();
			}
			return null;
		}
		
		@Override
		boolean contains(int hash, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			return entries[index] != null;
		}
		
		@Override
		void clear(byte level) {
			for (int i = 0; i < entries.length; i++) {
				JudyEntry<E> entry = entries[i];
				entry.setElement(null);
			}
			offHeapService.freeArray(entries);
			setEntries(null);
		}
		
	}
	
	/**
	 * Root node for Judy tree based indexing nodes
	 */
	static class JudyTree<E> {

		JudyIntermediateNode<E>[] nodes;
		JudyEntry<E> firstEntry;
		JudyEntry<E> lastEntry;
		volatile int size;
		
		JudyTree() {
			init();
		}
		
		@SuppressWarnings("unchecked")
		void init() {
			// Create and initialize first level nodes
			setNodes(offHeapService.newArray(JudyIntermediateNode[].class, NODE_SIZE));
			for (int i = 0 ; i < nodes.length; i++) {
				nodes[i].setRoot(this);
			}
		}
		
		JudyIntermediateNode<E>[] getNodes() {
			return nodes;
		}
		
		void setNodes(JudyIntermediateNode<E>[] nodes) {
			directMemoryService.setObjectField(this, NODES_FIELD_OFFSET, nodes);
		}
		
		JudyEntry<E> getFirstEntry() {
			return firstEntry;
		}
		
		void setFirstEntry(JudyEntry<E> firstEntry) {
			directMemoryService.setObjectField(this, FIRST_ENTRY_FIELD_OFFSET, firstEntry);
		}
		
		JudyEntry<E> getLastEntry() {
			return lastEntry;
		}
		
		void setLastEntry(JudyEntry<E> lastEntry) {
			directMemoryService.setObjectField(this, LAST_ENTRY_FIELD_OFFSET, lastEntry);
		}

		E add(E element, Lock judyTreeLock) {
			// Use most significant byte as first level index
			short index = (short)((element.hashCode() >> 24) & 0x000000FF);
			E obj = nodes[index].add(element.hashCode(), element, (byte) 1, judyTreeLock);
			if (obj == null) {
				size++;
			}
			return obj;
		}
		
		E remove(E element, Lock judyTreeLock) {
			// Use most significant byte as first level index
			short index = (short)((element.hashCode() >> 24) & 0x000000FF);
			E obj = nodes[index].remove(element.hashCode(), (byte) 1, judyTreeLock);
			if (obj != null) {
				size--;
			}
			return obj;
		}
		
		boolean contains(E element) {
			// Use most significant byte as first level index
			short index = (short)((element.hashCode() >> 24) & 0x000000FF);
			return nodes[index].contains(element.hashCode(), (byte) 1);
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
