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

import tr.com.serkanozal.jillegal.offheap.config.provider.annotation.OffHeapIgnoreInstrumentation;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryServiceFactory;
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
@SuppressWarnings("restriction")
public class OffHeapJudyHashSet<E> extends AbstractSet<E> implements OffHeapSet<E> {

	private final static int BITS_IN_BYTE = 8;
	private final static int MAX_LEVEL = (Integer.SIZE / BITS_IN_BYTE) - 1; 
	private final static int NODE_SIZE = 256;
	
	private static final OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();
	private static final DirectMemoryService directMemoryService = DirectMemoryServiceFactory.getDirectMemoryService();
	
	private static final int ELEMENT_FIELD_OFFSET = 
			(int) JvmUtil.getUnsafe().objectFieldOffset(
					JvmUtil.getField(JudyEntry.class, "element"));
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
	
	private JudyTree<E> root;
	private Class<E> elementType;
	
	public OffHeapJudyHashSet() {
		this.root = createJudyTree();
	}
	
	public OffHeapJudyHashSet(Class<E> elementType) {
		this.elementType = elementType;
		this.root = createJudyTree();
	}

	JudyTree<E> createJudyTree() {
		JudyTree<E> judyTree = new JudyTree<E>();
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
	public synchronized boolean contains(Object element) {
		return root.contains((E) element);
	}
  
	@Override
	public synchronized boolean add(E element) {
		return root.add(element) == null;	
	}
	
	@Override
	public synchronized E put(E element) {
		return root.add(element);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean remove(Object element) {
		return root.remove((E) element) != null;	
	}
	
	@Override
	public synchronized void clear() {
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
				synchronized (OffHeapJudyHashSet.this) {
					JudyEntry<E> nextEntry = currentEntry;
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

		E element;
		JudyEntry<E> prev;
		JudyEntry<E> next;

		E getElement() {
			return element;		
		} 

		E setElement(E element) {
			E oldElement = getElement();
			directMemoryService.setObjectField(this, ELEMENT_FIELD_OFFSET, element);
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
	
	@OffHeapIgnoreInstrumentation
	static abstract class JudyNode<E> {
		
		abstract void init();
		abstract E add(JudyTree<E> root, int hash, E element, byte level);
		abstract E remove(JudyTree<E> root, int hash, byte level);
		abstract boolean contains(int hash, byte level);
		abstract void clear(byte level);
		
	}
	
	@OffHeapIgnoreInstrumentation
	static class JudyIntermediateNode<E> extends JudyNode<E> {
		
		JudyNode<E>[] children;
		
		@SuppressWarnings("unchecked")
		@Override
		void init() {
			setChildren(offHeapService.newArray(JudyNode[].class, NODE_SIZE, false));
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
		E add(JudyTree<E> root, int hash, E element, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode<E> child = children[index];
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
			return child.add(root, hash, element, nextLevel);
		}
		
		@Override
		E remove(JudyTree<E> root, int hash, byte level) {
			if (children == null) {
				return null;
			}
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode<E> child = children[index];
			if (child != null) {
				return child.remove(root, hash, nextLevel);
			}
			return null;
		}
		
		@Override
		boolean contains(int hash, byte level) {
			if (children == null) {
				return false;
			}
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
				offHeapService.freeArray(children);
				setChildren(null);
			}
		}
		
	}
	
	@OffHeapIgnoreInstrumentation
	static class JudyLeafNode<E> extends JudyNode<E> {
		
		JudyEntry<E>[] entries;

		@SuppressWarnings("unchecked")
		@Override
		void init() {
			setEntries(offHeapService.newArray(JudyEntry[].class, NODE_SIZE, false));
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
		E add(JudyTree<E> root, int hash, E element, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry<E> entry = entries[index];
			if (entry == null) {
				entry = offHeapService.newObject(JudyEntry.class); 
				directMemoryService.setArrayElement(entries, index, entry);
			}
			
			E oldElement = entry.setElement(element);
			
			if (root.getFirstEntry() == null) {
				root.setFirstEntry(entry);
			}
			JudyEntry<E> lastEntry = root.getLastEntry();
			if (lastEntry != null) {
				entry.setPrev(lastEntry);
				lastEntry.setNext(entry);
			}
			root.setLastEntry(entry);
			
			return oldElement;
		}
		
		@Override
		E remove(JudyTree<E> root, int hash, byte level) {
			if (entries == null) {
				return null;
			}
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry<E> entryToRemove = entries[index];
			if (entryToRemove != null) {
				directMemoryService.setArrayElement(entries, index, null); 
				E removedElement = entryToRemove.getElement();
				entryToRemove.setElement(null);
				
				JudyEntry<E> prevEntry = entryToRemove.getPrev();
				JudyEntry<E> nextEntry = entryToRemove.getNext();
				entryToRemove.setPrev(null);
				entryToRemove.setNext(null);
				if (entryToRemove == root.getFirstEntry()) {
					root.setFirstEntry(nextEntry);
				}
				if (entryToRemove == root.lastEntry) {
					root.setLastEntry(prevEntry);
				}
				if (prevEntry != null) {
					prevEntry.setNext(nextEntry);
				}
				if (nextEntry != null) {
					nextEntry.setPrev(prevEntry);	
				}

				offHeapService.freeObject(entryToRemove);
				return removedElement;
			}
			return null;
		}
		
		@Override
		boolean contains(int hash, byte level) {
			if (entries == null) {
				return false;
			}
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			return entries[index] != null;
		}
		
		@Override
		void clear(byte level) {
			for (int i = 0; i < entries.length; i++) {
				JudyEntry<E> entry = entries[i];
				if (entry == null) {
					continue;
				}
				entry.setElement(null);
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
	static class JudyTree<E> {

		JudyIntermediateNode<E>[] nodes;
		JudyEntry<E> firstEntry;
		JudyEntry<E> lastEntry;
		volatile int size;

		@SuppressWarnings("unchecked")
		void init() {
			// Create and initialize first level nodes
			setNodes(offHeapService.newArray(JudyIntermediateNode[].class, NODE_SIZE, true));
		}
		
		JudyIntermediateNode<E>[] getNodes() {
			return nodes;
		}
		
		void setNodes(JudyIntermediateNode<E>[] nodes) {
			this.nodes = nodes;
		}
		
		JudyEntry<E> getFirstEntry() {
			return firstEntry;
		}
		
		void setFirstEntry(JudyEntry<E> firstEntry) {
			this.firstEntry = firstEntry;
		}
		
		JudyEntry<E> getLastEntry() {
			return lastEntry;
		}
		
		void setLastEntry(JudyEntry<E> lastEntry) {
			this.lastEntry = lastEntry;
		}

		E add(E element) {
			// Use most significant byte as first level index
			short index = (short)((element.hashCode() >> 24) & 0x000000FF);
			E obj = nodes[index].add(this, element.hashCode(), element, (byte) 1);
			if (obj == null) {
				size++;
			}
			return obj;
		}
		
		E remove(E element) {
			// Use most significant byte as first level index
			short index = (short)((element.hashCode() >> 24) & 0x000000FF);
			E obj = nodes[index].remove(this, element.hashCode(), (byte) 1);
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
