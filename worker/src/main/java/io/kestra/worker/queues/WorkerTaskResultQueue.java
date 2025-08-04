package io.kestra.worker.queues;

import io.kestra.core.runners.WorkerTaskResult;

public interface WorkerTaskResultQueue extends WorkerQueue<WorkerTaskResult> {
    
    /**
     * The default {@link WorkerTaskResultQueue} implementation
     */
    class Default extends AbstractDelegateWorkerQueue<WorkerTaskResult> implements WorkerTaskResultQueue {
        public Default(final WorkerQueue<WorkerTaskResult> queue) {
            super(queue);
        }
    }
}
