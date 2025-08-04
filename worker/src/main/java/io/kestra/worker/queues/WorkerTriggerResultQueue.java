package io.kestra.worker.queues;

import io.kestra.core.runners.WorkerTriggerResult;

/**
 * Typed worker queue for {@link WorkerTriggerResult}.
 */
public interface WorkerTriggerResultQueue extends WorkerQueue<WorkerTriggerResult> {
    
    /**
     * The default {@link WorkerTriggerResultQueue} implementation
     */
    class Default extends AbstractDelegateWorkerQueue<WorkerTriggerResult> implements WorkerTriggerResultQueue{
        public Default(final WorkerQueue<WorkerTriggerResult> queue) {
            super(queue);
        }
    }
}
