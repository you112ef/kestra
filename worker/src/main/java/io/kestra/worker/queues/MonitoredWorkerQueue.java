package io.kestra.worker.queues;

import io.kestra.core.metrics.MetricRegistry;
import io.micrometer.core.instrument.Counter;

import java.time.Duration;

public class MonitoredWorkerQueue<T> extends AbstractDelegateWorkerQueue<T> {
    
    public static final String QUEUE_SIZE = "queue.size";
    public static final String QUEUE_REMAINING_CAPACITY = "queue.remaining.capacity";
    public static final String QUEUE_ENQUEUED = "queue.enqueued";
    public static final String QUEUE_DEQUEUED = "queue.dequeued";
    private final MetricRegistry metricRegistry;
    
    private final Counter enqueuedCounter;
    private final Counter dequeuedCounter;
    
    public MonitoredWorkerQueue(MetricRegistry metricRegistry, String  queueName, WorkerQueue<T> queue) {
        super(queue);
        this.metricRegistry = metricRegistry;
        
        String[] tags = new String[]{"name", queueName};
        this.metricRegistry.registerGauge(QUEUE_SIZE, "Current number of items in the queue", this::size, tags);
        this.metricRegistry.registerGauge(QUEUE_REMAINING_CAPACITY, "Remaining capacity in the queue", this::size, tags);
        this.enqueuedCounter = this.metricRegistry.counter(QUEUE_ENQUEUED, "Number of items enqueued", tags);
        this.dequeuedCounter = this.metricRegistry.counter(QUEUE_DEQUEUED, "Number of items dequeued", tags);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public T poll(Duration timeout) throws InterruptedException {
        T item = super.poll(timeout);
        dequeuedCounter.increment();
        return item;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void put(T event) {
        super.put(event);
        enqueuedCounter.increment();
    }
}
