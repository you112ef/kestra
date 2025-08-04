package io.kestra.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A WorkerIO thread is responsible for processing incoming/outgoing data from and to the worker.
 * <p>
 * A WorkerIO mostly does network operations.
 */
public abstract class WorkerIOThread implements Runnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(WorkerIOThread.class);
    
    private final String name;
    protected String workerId;
    protected String workerGroup;
    
    private volatile Thread thread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final CountDownLatch stopped = new CountDownLatch(1);
    
    public WorkerIOThread(final String name) {
        this.name = Objects.requireNonNull(name, "name must not be null");
    }
    
    public synchronized void start(final String workerId, final String workerGroup) {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("[%s] already started".formatted(getClass().getSimpleName()));
        }
        
        this.workerId = workerId;
        this.workerGroup = workerGroup;
        // TODO could be probably replace with a virtual-thread executor
        this.thread = new Thread(this, "worker-" + this.name + "-" + workerId);
        this.thread.setDaemon(false);
        this.thread.start();
        
        LOG.info("[{}] started with workerId={} group={}", getClass().getSimpleName(), workerId, workerGroup);
    }
    
    @Override
    public void run() {
        try {
            while (running.get()) {
                try {
                    doOnLoop();
                } catch (InterruptedException ie) {
                    LOG.info("[{}] interrupted, stopping", getClass().getSimpleName());
                    Thread.currentThread().interrupt();
                    break; // exit loop
                } catch (Exception e) {
                    LOG.error("Error in IO worker loop", e);
                }
            }
        } finally {
            stopped.countDown();
            LOG.info("[{}] stopped", getClass().getSimpleName());
        }
    }
    
    protected abstract void doOnLoop() throws Exception;
    
    protected void doOnStop() {
        //noop
    }
    
    public synchronized void stop() {
        if (!running.compareAndSet(true, false)) {
            LOG.debug("[{}] stop() called but not running", getClass().getSimpleName());
            return;
        }
        
        if (thread != null) {
            try {
                doOnStop();
            } catch (Exception e) {
                LOG.error("Error in IO worker loop", e);
            }
            thread.interrupt();
            try {
                if (!stopped.await(1, TimeUnit.MINUTES)) {
                    LOG.warn("Timeout while waiting for {} to complete", thread.getName());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
