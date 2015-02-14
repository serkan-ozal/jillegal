/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.memory.allocator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.util.JvmUtil;

@SuppressWarnings( { "restriction" } )
public class BatchMemoryAllocator implements MemoryAllocator {

	private static final Unsafe UNSAFE = JvmUtil.getUnsafe();
	
	private static final AtomicLongFieldUpdater<BatchMemoryAllocator> TOTAL_UPDATER = 
			AtomicLongFieldUpdater.newUpdater(BatchMemoryAllocator.class, "total");
	private static final AtomicLongFieldUpdater<BatchMemoryAllocator> USED_UPDATER = 
			AtomicLongFieldUpdater.newUpdater(BatchMemoryAllocator.class, "used");

	private volatile long total;
	private volatile long used;
	
	private final List<Segment> segments = new ArrayList<Segment>();
	
	public BatchMemoryAllocator() {
		init();
	}
	
	private void init() {
		// Pre-allocate first segment
		segments.add(new Segment());
	}
	
	@Override
	public synchronized long allocateMemory(long size) {
		if (size <= 0 || size > Segment.MAX_SEGMENT_SIZE) {
			throw new IllegalArgumentException("Only sizes between 0 < x <= " +  Segment.MAX_SEGMENT_SIZE + 
					" are supported right now !");
		}
		for (int i = 0; i < segments.size(); i++) {
			Segment segment = segments.get(i);
			if (segment.hasAvailableMemory((int) size)) {
				try {
					return segment.allocate((int) size);
				} 
				catch (IllegalStateException e) {
					// Possibly has enough free memory but cannot allocate as continuous memory
				}
			}
		}
		// Create a new segment and allocate from it
		Segment segment = new Segment();
		segments.add(segment);
		return segment.allocate((int) size);
	}
	
	@Override
	public synchronized void freeMemory(long address) {
		for (int i = 0; i < segments.size(); i++) {
			Segment segment = segments.get(i);
			if (segment.isMine(address)) {
				segment.free(address);
				break;
			}
		}
		disposeFreeSegments();
	}
	
	@Override
	public long totalMemory() {
		return total;
	}
	
	@Override
	public long usedMemory() {
		return used;
	}

	private void disposeFreeSegments() {
		List<Segment> destroyedSegments = null;
		for (int i = 0; i < segments.size(); i++) {
			Segment segment = segments.get(i);
			if (segment.isEmpty()) {
				if (destroyedSegments == null) {
					destroyedSegments = new ArrayList<Segment>();
				}
				segment.destroy();
				destroyedSegments.add(segment);
			}
		}
		if (destroyedSegments != null) {
			segments.removeAll(destroyedSegments);
		}
	}
	
	class Segment {
		
		static final int MAX_SEGMENT_SIZE = Integer.MAX_VALUE;
		static final int DEFAULT_CHUNK_SIZE = 1024;
		static final int DEFAULT_SEGMENT_SIZE = 10 * 1024 * 1024;
		static final byte CHUNK_IS_FREE = 0x00;
		static final byte CHUNK_IS_FULL = (byte) 0xFF;
		
		int chunkSize;
		int segmentSize;
		byte[] chunks;
		int[] sizes;
		AtomicInteger used = new AtomicInteger();
		long allocatedAddress;
		
		Segment() {
			this.chunkSize = DEFAULT_CHUNK_SIZE;
			this.segmentSize = DEFAULT_SEGMENT_SIZE;
			init();
		}
		
		Segment(int chunkSize, int segmentSize) {
			this.chunkSize = chunkSize;
			this.segmentSize = segmentSize;
			init();
		}
		
		void init() {
			int chunkCount = segmentSize / chunkSize;
			if (segmentSize % chunkSize != 0) {
				chunkCount++;
			}
			this.chunks = new byte[chunkCount];
			for (int i = 0; i < chunkCount; i++) {
				chunks[i] = CHUNK_IS_FREE;
			}
			this.sizes = new int[chunkCount];
			for (int i = 0; i < chunkCount; i++) {
				sizes[i] = 0;
			}
			this.allocatedAddress = UNSAFE.allocateMemory(segmentSize);
			UNSAFE.setMemory(allocatedAddress, segmentSize, (byte) 0x00);
			TOTAL_UPDATER.addAndGet(BatchMemoryAllocator.this, segmentSize);
		}
		
		boolean isActive() {
			return chunks != null;
		}
		
		boolean isEmpty() {
			return used.get() == 0;
		}
		
		boolean isFull() {
			return used.get() == segmentSize;
		}
		
		boolean hasAvailableMemory(int size) {
			return segmentSize - used.get() >= size; 
		}
		
		boolean isMine(long address) {
			return address >= allocatedAddress && address <= (allocatedAddress + segmentSize);
		}
		
		long allocate(int size) {
			if (!hasAvailableMemory(size)) {
				throw new IllegalStateException("Not enough memory for this segment for size " + size + 
						". Used: " + used + ", size: " + segmentSize);
			}
			
			int requiredChunkCount = (size / chunkSize);
			if (size % chunkSize != 0) {
				requiredChunkCount++;
			}

			int firstAllocatedChunk = -1;
			for (int i = 0; i < chunks.length; i++) {
				// If current chunk is free and there are enough chunks to check
				if ((chunks[i] == CHUNK_IS_FREE) && (i + requiredChunkCount) < chunks.length) {
					boolean ok = true;
					// Check for next chunks are also available or not
					for (int j = 1; j < requiredChunkCount; j++) {
						if (chunks[i + j] != CHUNK_IS_FREE) {
							ok = false;
							break;
						}
					}
					// If next chunks are also available
					if (ok) {
						firstAllocatedChunk = i;
						break;
					}
				}	
			}
			
			// If any continuous chunks couldn't be found
			if (firstAllocatedChunk == -1) {
				throw new IllegalStateException("Couldn't find enough continuous memory for this segment for size " + size + 
						". Used: " + used + ", size: " + segmentSize);
			} 
			
			// Mark related chunks as allocated
			for (int i = 0; i < requiredChunkCount; i++) {
				chunks[firstAllocatedChunk + i] = CHUNK_IS_FULL;
			}
			
			// Calculate allocated size
			int allocatedSize = requiredChunkCount * chunkSize;
			
			// Store the size of allocated memory
			sizes[firstAllocatedChunk] = allocatedSize;
			for (int i = 1; i < requiredChunkCount; i++) {
				sizes[firstAllocatedChunk + i] = 0;
			}
			
			// Update used memory count
			used.addAndGet(allocatedSize);
			
			USED_UPDATER.addAndGet(BatchMemoryAllocator.this, allocatedSize);
			
			// Return absolute address
			return allocatedAddress + (firstAllocatedChunk * chunkSize);
		}
		
		void free(long address) {
			int firstAllocatedChunk = (int) (address - allocatedAddress) / chunkSize;
			int allocatedSize = sizes[firstAllocatedChunk];
			
			if (allocatedSize <= 0) {
				throw new IllegalArgumentException("Invalid address to free: " + address);
			}
			
			int allocatedChunkCount = allocatedSize / chunkSize;
			
			// Mark related chunks as free
			for (int i = 0; i < allocatedChunkCount; i++) {
				chunks[firstAllocatedChunk + i] = CHUNK_IS_FREE;
			}
			
			// Clear sizes of free memory
			for (int i = 0; i < allocatedChunkCount; i++) {
				sizes[firstAllocatedChunk + i] = 0;
			}
			
			// Update used memory count
			used.addAndGet(-allocatedSize);
			
			USED_UPDATER.addAndGet(BatchMemoryAllocator.this, -allocatedSize);
		}
		
		void destroy() {
			UNSAFE.freeMemory(allocatedAddress);
			TOTAL_UPDATER.addAndGet(BatchMemoryAllocator.this, -segmentSize);
			chunks = null;
			sizes = null;
		}
		
	}
	
}
