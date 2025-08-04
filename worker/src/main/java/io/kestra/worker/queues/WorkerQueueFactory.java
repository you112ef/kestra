package io.kestra.worker.queues;

import io.kestra.core.metrics.MetricRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class WorkerQueueFactory {
    
    public static final int DEFAULT_QUEUE_SIZE = 5000;
    
    private final Map<QueueKey, WorkerQueue<?>> queues;
    
    private final MetricRegistry metricRegistry;
    
    @Inject
    public WorkerQueueFactory(final MetricRegistry metricRegistry) {
        this.queues = new ConcurrentHashMap<>();
        this.metricRegistry = metricRegistry;
    }
    
    @SuppressWarnings("unchecked")
    public synchronized <T> WorkerQueue<T> getOrCreate(final String workerId, final Class<T> type) {
        QueueKey key = new QueueKey(workerId, type);
        return (WorkerQueue<T>) queues.computeIfAbsent(key, unused ->
            new MonitoredWorkerQueue<T>(metricRegistry, type.getSimpleName().toLowerCase(),
                new InMemoryWorkerQueue<>(DEFAULT_QUEUE_SIZE)
            )
        );
    }
    
    private record QueueKey(String workerId, Class<?> type) {
    }
}
