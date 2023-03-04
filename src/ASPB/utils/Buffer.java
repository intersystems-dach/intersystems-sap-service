package ASPB.utils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Buffer<T> {

    // https://www.baeldung.com/java-concurrent-queues
    // LinkedBlockingQueue
    // ConcurrentLinkedQueue

    private Queue<T> queue;

    private int maxBufferSize;

    public Buffer(int size) {
        if (size > 0)
            maxBufferSize = size;
        else
            maxBufferSize = -1;

        queue = new ConcurrentLinkedQueue<T>();
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
