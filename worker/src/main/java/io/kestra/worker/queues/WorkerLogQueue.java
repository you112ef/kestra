package io.kestra.worker.queues;

import io.kestra.core.models.executions.LogEntry;
import io.kestra.core.models.executions.MetricEntry;
import io.kestra.core.runners.WorkerTriggerResult;

import java.time.Duration;
import java.util.Objects;

public interface WorkerLogQueue extends WorkerQueue<LogEntry>{
    
    /**
     * The default {@link LogEntry} implementation
     */
    class Default extends AbstractDelegateWorkerQueue<LogEntry> implements WorkerLogQueue {
        public Default(final WorkerQueue<LogEntry> queue) {
            super(queue);
        }
    }
}
