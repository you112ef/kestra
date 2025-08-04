package io.kestra.worker.processors;

import io.kestra.core.metrics.MetricRegistry;
import io.kestra.core.models.executions.LogEntry;
import io.kestra.core.models.executions.MetricEntry;
import io.kestra.core.runners.RunContextInitializer;
import io.kestra.core.runners.RunContextLoggerFactory;
import io.kestra.core.runners.Worker;
import io.kestra.core.runners.WorkerJob;
import io.kestra.core.runners.WorkerSecurityService;
import io.kestra.core.runners.WorkerTask;
import io.kestra.core.runners.WorkerTaskResult;
import io.kestra.core.runners.WorkerTrigger;
import io.kestra.core.runners.WorkerTriggerResult;
import io.kestra.core.server.Metric;
import io.kestra.core.services.LogService;
import io.kestra.core.services.VariablesService;
import io.kestra.core.trace.Tracer;
import io.kestra.core.trace.TracerFactory;
import io.kestra.worker.queues.WorkerLogQueue;
import io.kestra.worker.queues.WorkerMetricQueue;
import io.kestra.worker.queues.WorkerQueueFactory;
import io.kestra.worker.queues.WorkerTaskResultQueue;
import io.kestra.worker.queues.WorkerTriggerResultQueue;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class WorkerJobProcessorFactory {
    
    @Inject
    private LogService logService;
    @Inject
    private MetricRegistry metricRegistry;
    @Inject
    private WorkerSecurityService workerSecurityService;
    @Inject
    private RunContextInitializer runContextInitializer;
    @Inject
    private RunContextLoggerFactory runContextLoggerFactory;
    @Inject
    private VariablesService variablesService;
    
    // QUEUES
    @Inject
    private WorkerQueueFactory workerQueueFactory;
    
    @Inject
    private TracerFactory tracerFactory;
    private Tracer tracer;
    
    @PostConstruct
    public void init() {
        this.tracer = tracerFactory.getTracer(Worker.class, "WORKER");
    }
    
    @SuppressWarnings("unchecked")
    public <T extends WorkerJob> WorkerJobProcessor<T> create(String workerId,
                                                              String workerGroup,
                                                              T job) {
        if (job instanceof WorkerTask) {
            return (WorkerJobProcessor<T>) new WorkerTaskProcessor(
                workerId,
                workerGroup,
                logService,
                metricRegistry,
                workerSecurityService,
                tracer,
                variablesService,
                runContextInitializer,
                runContextLoggerFactory,
                new WorkerTaskResultQueue.Default(workerQueueFactory.getOrCreate(workerId, WorkerTaskResult.class)),
                new WorkerMetricQueue.Default(workerQueueFactory.getOrCreate(workerId, MetricEntry.class))
            );
        } else if (job instanceof WorkerTrigger) {
            return (WorkerJobProcessor<T>) new WorkerTriggerProcessor(
                workerGroup,
                logService,
                metricRegistry,
                workerSecurityService,
                tracer,
                runContextInitializer,
                new WorkerLogQueue.Default(workerQueueFactory.getOrCreate(workerId, LogEntry.class)),
                new WorkerTriggerResultQueue.Default(workerQueueFactory.getOrCreate(workerId, WorkerTriggerResult.class))
            );
        }
        
        throw new IllegalArgumentException("Unsupported worker job type [" + job.getClass().getName() + "]");
    }
    
}
