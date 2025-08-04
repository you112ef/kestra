package io.kestra.worker;

import io.kestra.core.runners.WorkerJob;
import io.kestra.core.utils.ExecutorsUtils;
import io.kestra.worker.processors.WorkerJobProcessor;
import io.kestra.worker.processors.WorkerJobProcessorFactory;
import io.kestra.worker.queues.WorkerJobQueue;
import io.kestra.worker.queues.WorkerQueueFactory;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Components responsible for executing {@link io.kestra.core.runners.WorkerJob}
 */
@Prototype
@Slf4j
public class WorkerJobExecutor {
    
    private static final String EXECUTOR_NAME = "worker";
    
    private final WorkerQueueFactory workerQueueFactory;
    private final WorkerJobProcessorFactory workerJobProcessorFactory;
    private final ExecutorsUtils executorsUtils;
    
    private ExecutorService executorService;
    private List<WorkerJobConsumer> workerJobConsumers;
    
    private final AtomicBoolean started = new AtomicBoolean(false);
    
    @Inject
    public WorkerJobExecutor(final WorkerQueueFactory workerQueueFactory,
                             final ExecutorsUtils executorsUtils,
                             final WorkerJobProcessorFactory workerJobProcessorFactory) {
        this.workerJobProcessorFactory = workerJobProcessorFactory;
        this.workerQueueFactory = workerQueueFactory;
        this.executorsUtils = executorsUtils;
    }
    
    public void start(final String workerId,
                      final String workerGroup,
                      int threads) {
        WorkerJobQueue workerJobQueue = new WorkerJobQueue.Default(workerQueueFactory.getOrCreate(workerId, WorkerJob.class));
        if (this.started.compareAndSet(false, true)) {
            this.executorService = executorsUtils.maxCachedThreadPool(threads, EXECUTOR_NAME);
            this.workerJobConsumers = new ArrayList<>(threads);
            for (int i = 0; i < threads; i++) {
                WorkerJobConsumer consumer = new WorkerJobConsumer(
                    workerJobQueue,
                    workerJobProcessorFactory,
                    workerId,
                    workerGroup
                );
                this.workerJobConsumers.add(consumer);
                executorService.submit(consumer);
            }
        } else {
            throw new IllegalStateException("already started");
        }
    }
    
    /**
     * Returns the number of running a job.
     *
     * @return the number of job being processed
     */
    public long getRunningJobCount() {
        return workerJobConsumers.stream()
            .filter(WorkerJobConsumer::isProcessing)
            .count();
    }
    
    /**
     * Notify all underlying WorkerJob consumers to pause.
     */
    public void pause() {
        workerJobConsumers.forEach(WorkerJobConsumer::pause);
    }
    
    /**
     * Notify all underlying WorkerJob consumers to resume.
     */
    public void resume() {
        checkIsStarted();
        workerJobConsumers.forEach(WorkerJobConsumer::resume);
    }
    
    private void checkIsStarted() {
        if (!this.started.get()) {
            throw new IllegalStateException("WorkerJobExecutor not started");
        }
    }
    
    /**
     * Immediately initiates shutdown of all consumers and halts the processing of waiting jobs.
     * <p>
     * This is a convenience method that calls {@link #shutdown(Duration)} with {@code Duration.ZERO}
     * and ignores any {@link InterruptedException} by resetting the interrupt flag.
     */
    public void shutdownNow() {
        try {
            shutdown(Duration.ZERO);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Initiates a graceful shutdown by notifying all consumers to stop and waiting for termination.
     * <p>
     * If the specified {@code terminationGracePeriod} is {@code null} or {@code Duration.ZERO},
     * the executor will skip graceful shutdown and immediately attempt to forcefully stop all
     * running tasks.
     *
     * @param terminationGracePeriod the maximum duration to wait for graceful shutdown
     * @return {@code true} if the executor terminated within the timeout; {@code false} if forced shutdown was required
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public boolean shutdown(final Duration terminationGracePeriod) throws InterruptedException {
        if (!this.started.compareAndSet(true, false)) {
            return true; // Already shut down or not started.
        }
        
        // Initiate graceful shutdown
        this.executorService.shutdown();
        
        // Notify all WorkerJobConsumers to stop
        this.workerJobConsumers.forEach(WorkerJobConsumer::stop);
        
        if (terminationGracePeriod == null || terminationGracePeriod.equals(Duration.ZERO)) {
            this.executorService.shutdownNow();
            return false;
        }
        
        // Wait for all WorkerJobConsumers to terminate
        boolean terminated = this.executorService.awaitTermination(
            terminationGracePeriod.toMillis(), TimeUnit.MILLISECONDS);
        
        if (!terminated) {
            log.warn("Worker still has pending jobs after the termination grace period. Forcing shutdown.");
            this.executorService.shutdownNow();
        }
        
        return terminated;
    }
    
    private static class WorkerJobConsumer implements Runnable {
        
        private final AtomicBoolean stopped = new AtomicBoolean(false);
        private final AtomicBoolean paused = new AtomicBoolean(false);
        
        private final ReentrantLock pauseLock = new ReentrantLock();
        private final Condition unpaused = pauseLock.newCondition();
        
        private final AtomicReference<WorkerJobProcessor<WorkerJob>> running = new AtomicReference<>(null);
        
        private final WorkerJobQueue workerJobQueue;
        private final WorkerJobProcessorFactory workerJobProcessorFactory;
        private final String workerId;
        private final String workerGroup;
        
        public WorkerJobConsumer(WorkerJobQueue workerJobQueue,
                                 WorkerJobProcessorFactory workerJobProcessorFactory,
                                 String workerId,
                                 String workerGroup) {
            this.workerJobQueue = workerJobQueue;
            this.workerJobProcessorFactory = workerJobProcessorFactory;
            this.workerId = workerId;
            this.workerGroup = workerGroup;
        }
        
        /**
         * Continuously polls for new {@link WorkerJob} and processes them sequentially.
         * <p>
         * It blocks while waiting for new jobs and ensures that only one job is processed
         * at a time. This method will not return unless interrupted or explicitly stopped.
         */
        @Override
        public void run() {
            try {
                while (!stopped.get()) {
                    waitIfPaused();
                    
                    WorkerJob job = workerJobQueue.poll(Duration.ofSeconds(1));
                    if (job == null || stopped.get()) {
                        continue;
                    }
                    
                    try {
                        WorkerJobProcessor<WorkerJob> processor =
                            workerJobProcessorFactory.create(workerId, workerGroup, job);
                        
                        running.set(processor);
                        processor.process(job);
                    } finally {
                        running.set(null);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        /**
         * Check whether a job is currently being processed
         *
         * @return {@code true} if a {@link WorkerJob} is actively being processed; {@code false} otherwise.
         */
        public boolean isProcessing() {
            return running.get() != null;
        }
        
        private void waitIfPaused() throws InterruptedException {
            pauseLock.lock();
            try {
                while (paused.get() && !stopped.get()) {
                    unpaused.await(); // Wait until resume() signals
                }
            } finally {
                pauseLock.unlock();
            }
        }
        
        /**
         * Pauses polling for new {@link WorkerJob} instances.
         * <p>
         * If a job is currently running, it will continue to completion.
         * No new jobs will be polled until resumed.
         */
        public void pause() {
            paused.set(true);
        }
        
        /**
         * Resumes polling and processing of {@link WorkerJob} instances if currently paused.
         */
        public void resume() {
            pauseLock.lock();
            try {
                if (paused.compareAndSet(true, false)) {
                    unpaused.signalAll();
                }
            } finally {
                pauseLock.unlock();
            }
        }
        
        /**
         * Stops polling and processing of {@link WorkerJob} instances.
         */
        public void stop() {
            if (this.stopped.compareAndSet(false, true)) {
                resume(); // In case it's paused and blocked
                
                WorkerJobProcessor<WorkerJob> processor = running.get();
                if (processor != null) {
                    processor.stop();
                }
            }
        }
    }
}
