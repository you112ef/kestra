package io.kestra.worker.queues;

import io.kestra.core.models.executions.MetricEntry;

public interface WorkerMetricQueue extends WorkerQueue<MetricEntry> {
    
    /**
     * The default {@link MetricEntry} implementation
     */
    class Default extends AbstractDelegateWorkerQueue<MetricEntry> implements WorkerMetricQueue {
        public Default(final WorkerQueue<MetricEntry> queue) {
            super(queue);
        }
    }
}
