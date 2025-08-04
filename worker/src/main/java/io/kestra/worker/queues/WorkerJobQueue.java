package io.kestra.worker.queues;

import io.kestra.core.runners.WorkerJob;

/**
 * 
 */
public interface WorkerJobQueue extends WorkerQueue<WorkerJob> {
    
    /**
     * The default {@link WorkerJob} implementation
     */
    class Default extends AbstractDelegateWorkerQueue<WorkerJob> implements WorkerJobQueue {
        public Default(final WorkerQueue<WorkerJob> queue) {
            super(queue);
        }
    }
}
