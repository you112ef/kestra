package io.kestra.worker.queues;

import java.time.Duration;

/**
 * Represents an event queue used for worker intra-processes communication.
 * <p>
 * Implementations of this interface are expected to be in-memory oriented. 
 *  
 * @param <T> type of the queue.
 */
public interface WorkerQueue<T> {
    
    T poll(Duration timeout) throws InterruptedException;
    
    void put(T event);
    
    int remainingCapacity();
    
    int capacity();
    
    int size();
}
