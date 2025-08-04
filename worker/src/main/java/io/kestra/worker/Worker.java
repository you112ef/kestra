package io.kestra.worker;

import io.kestra.core.metrics.MetricRegistry;
import io.kestra.core.server.Metric;
import io.kestra.core.server.ServerConfig;
import io.kestra.core.server.Service;
import io.kestra.core.server.ServiceStateChangeEvent;
import io.kestra.core.server.ServiceType;
import io.kestra.core.services.WorkerGroupService;
import io.kestra.core.utils.Await;
import io.kestra.server.AbstractService;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.event.ApplicationEventPublisher;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.kestra.core.server.Service.ServiceState.TERMINATED_FORCED;
import static io.kestra.core.server.Service.ServiceState.TERMINATED_GRACEFULLY;

@SuppressWarnings("this-escape")
@Slf4j
@Prototype
public class Worker extends AbstractService implements Service {
    
    private static final String SERVICE_PROPS_WORKER_GROUP = "worker.group";
    
    @Inject
    private MetricRegistry metricRegistry;
    
    @Inject
    private ServerConfig serverConfig;
    
    @Getter
    private final Map<Long, AtomicInteger> metricRunningCount = new ConcurrentHashMap<>();
    
    private final AtomicBoolean skipGracefulTermination = new AtomicBoolean(false);
    
    private final WorkerGroupService workerGroupService;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    private final AtomicInteger pendingJobCount = new AtomicInteger(0);
    private final AtomicInteger runningJobCount = new AtomicInteger(0);
    
    private final WorkerJobExecutor workerJobExecutor;
    private final WorkerJobFetcher workerJobFetcher;
    private final WorkerTaskResultSender workerTaskResultSender;
    
    private String workerGroup;
    
    /**
     * Creates a new {@link Worker} instance.
     */
    @Inject
    public Worker(
        ApplicationEventPublisher<ServiceStateChangeEvent> eventPublisher,
        WorkerGroupService workerGroupService,
        WorkerJobExecutor workerJobExecutor,
        WorkerJobFetcher workerJobFetcher,
        WorkerTaskResultSender workerTaskResultSender
    ) {
        super(ServiceType.WORKER_AGENT, eventPublisher);
        this.workerGroupService = workerGroupService;
        this.workerJobExecutor = workerJobExecutor;
        this.workerJobFetcher = workerJobFetcher;
        this.workerTaskResultSender = workerTaskResultSender;
        this.setState(ServiceState.CREATED);
    }
    
    @Override
    public Set<Metric> getMetrics() {
        if (this.metricRegistry == null) {
            // can arrive if called before the instance is fully created
            return Collections.emptySet();
        }
        
        Stream<String> metrics = Stream.of(
            MetricRegistry.METRIC_WORKER_JOB_THREAD_COUNT,
            MetricRegistry.METRIC_WORKER_JOB_PENDING_COUNT,
            MetricRegistry.METRIC_WORKER_JOB_RUNNING_COUNT
        );
        
        return metrics
            .flatMap(metric -> Optional.ofNullable(metricRegistry.findGauge(metric)).stream())
            .map(Metric::of)
            .collect(Collectors.toSet());
    }
    
    public void start(int numThreads, String workerGroupKey) {
        if (!this.initialized.compareAndSet(false, true)) {
            throw new IllegalStateException("Worker already started");
        }
        
        this.workerGroup = workerGroupService.resolveGroupFromKey(workerGroupKey);
        
        String[] tags = workerGroup == null ? new String[0] : new String[]{MetricRegistry.TAG_WORKER_GROUP, workerGroup};
        // create metrics to store thread count, pending jobs and running jobs, so we can have autoscaling easily
        this.metricRegistry.gauge(MetricRegistry.METRIC_WORKER_JOB_THREAD_COUNT, MetricRegistry.METRIC_WORKER_JOB_THREAD_COUNT_DESCRIPTION, numThreads, tags);
        this.metricRegistry.gauge(MetricRegistry.METRIC_WORKER_JOB_PENDING_COUNT, MetricRegistry.METRIC_WORKER_JOB_PENDING_COUNT_DESCRIPTION, pendingJobCount, tags);
        this.metricRegistry.gauge(MetricRegistry.METRIC_WORKER_JOB_RUNNING_COUNT, MetricRegistry.METRIC_WORKER_JOB_RUNNING_COUNT_DESCRIPTION, runningJobCount, tags);
        
        workerTaskResultSender.start(getId(), workerGroup);
        workerJobFetcher.start(getId(), workerGroup);
        workerJobExecutor.start(getId(), workerGroup, numThreads);
        
        if (workerGroupKey != null) {
            log.info("Worker started with {} thread(s) in group '{}'", numThreads, workerGroupKey);
        } else {
            log.info("Worker started with {} thread(s)", numThreads);
        }
        setState(ServiceState.RUNNING);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    protected Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(SERVICE_PROPS_WORKER_GROUP, workerGroup);
        return properties;
    }
    

    @Override
    protected ServiceState doStop() {
        this.workerJobFetcher.stop();
        this.workerJobExecutor.pause();
        this.workerTaskResultSender.stop();
        
        final boolean terminatedGracefully;
        if (!skipGracefulTermination.get()) {
            terminatedGracefully = waitForTasksCompletion(serverConfig.terminationGracePeriod());
        } else {
            log.info("Terminating now and skip waiting for tasks completions.");
            this.workerJobExecutor.shutdownNow();
            terminatedGracefully = false;
        }
        return terminatedGracefully ? TERMINATED_GRACEFULLY : TERMINATED_FORCED;
    }
    
    
    private boolean waitForTasksCompletion(final Duration timeout) {
        final Instant deadline = Instant.now().plus(timeout);
        
        AtomicReference<ServiceState> shutdownState = new AtomicReference<>();
        // start shutdown
        Thread.ofVirtual().name("worker-shutdown").start(
            () -> {
                try {
                    long remaining = Math.max(0, Instant.now().until(deadline, ChronoUnit.MILLIS));
                    boolean gracefullyShutdown = this.workerJobExecutor.shutdown(Duration.ofMillis(remaining));
                    shutdownState.set(gracefullyShutdown ? TERMINATED_GRACEFULLY : TERMINATED_FORCED);
                } catch (InterruptedException e) {
                    log.error("Failed to shutdown. Thread was interrupted");
                    shutdownState.set(TERMINATED_FORCED);
                }
            }
        );
        
        // wait for task completion
        Await.until(
            () -> {
                ServiceState serviceState = shutdownState.get();
                if (serviceState == TERMINATED_FORCED || serviceState == TERMINATED_GRACEFULLY) {
                    log.info("All working threads are terminated.");
                    return true;
                }
                
                long runningJobs = this.workerJobExecutor.getRunningJobCount();
                if (runningJobs == 0) {
                    log.debug("All worker threads is terminated.");
                } else {
                    log.warn("Waiting for all worker threads to terminate (remaining: {}).", runningJobs);
                }
                
                return false;
            },
            Duration.ofSeconds(1)
        );
        
        return shutdownState.get() == TERMINATED_GRACEFULLY;
    }
    
    /**
     * Specify whether to skip graceful termination on shutdown.
     *
     * @param skipGracefulTermination {@code true} to skip graceful termination on shutdown.
     */
    @Override
    public void skipGracefulTermination(final boolean skipGracefulTermination) {
        this.skipGracefulTermination.set(skipGracefulTermination);
    }
}
