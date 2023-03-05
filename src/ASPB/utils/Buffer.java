package ASPB.utils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A generic buffer class that can be used to store items of any type.
 * The buffer is thread-safe and can be used by multiple threads at the same
 * time.
 * 
 * @author Philipp Bonin
 * @version 1.0
 */
public class Buffer<T> {

    // https://www.baeldung.com/java-concurrent-queues
    // LinkedBlockingQueue
    // ConcurrentLinkedQueue

    private Queue<T> queue;

    private int maxBufferSize;

    /**
     * Create a new buffer with a optional maximum size.
     * 
     * @param size The maximum size of the buffer. If the size is less than or equal
     *             to 0, the buffer has no maximum size.
     */
    public Buffer(int size) {
        if (size > 0)
            maxBufferSize = size;
        else
            maxBufferSize = -1;

        queue = new ConcurrentLinkedQueue<T>();

        Logger.log("Buffer created with " + (maxBufferSize == -1 ? "unlimited size" : "size " + maxBufferSize));
    }

    /**
     * Create a new buffer with no maximum size.
     */
    public Buffer() {
        maxBufferSize = -1;
        queue = new ConcurrentLinkedQueue<T>();

        Logger.log("Buffer created with unlimited size");
    }

    /**
     * Add an item to the buffer. If the buffer is full, the item is not added and
     * false is returned.
     * 
     * @param item The item to add to the buffer
     * @return True if the item was added to the buffer, false otherwise
     */
    public boolean add(T item) {
        if (maxBufferSize > 0 && queue.size() >= maxBufferSize)
            return false;

        return queue.add(item);
    }

    /**
     * Remove an item from the buffer. If the buffer is empty, null is returned.
     * 
     * @return The item removed from the buffer, or null if the buffer is empty
     */
    public T poll() {
        return queue.poll();
    }

    /**
     * Get the number of items in the buffer.
     * 
     * @return The number of items in the buffer
     */
    public int size() {
        return queue.size();
    }
}
