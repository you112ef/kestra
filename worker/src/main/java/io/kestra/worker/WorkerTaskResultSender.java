package io.kestra.worker;

import io.grpc.stub.StreamObserver;
import io.kestra.core.contexts.KestraContext;
import io.kestra.core.runners.WorkerJob;
import io.kestra.core.runners.WorkerTaskResult;
import io.kestra.server.GrpcChannelProvider;
import io.kestra.server.grpc.RequestOrResponseHeader;
import io.kestra.server.internals.BatchMessage;
import io.kestra.server.internals.MessageFormats;
import io.kestra.worker.grpc.WorkerControllerServiceGrpc;
import io.kestra.worker.grpc.WorkerJobResultsRequest;
import io.kestra.worker.grpc.WorkerJobResultsResponse;
import io.kestra.worker.queues.WorkerJobQueue;
import io.kestra.worker.queues.WorkerQueueFactory;
import io.kestra.worker.queues.WorkerTaskResultQueue;
import io.kestra.worker.queues.WorkerTriggerResultQueue;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.UUID;

/**
 * Component responsible for fetching worker jobs.
 */
@Prototype
@Slf4j
public class WorkerTaskResultSender extends WorkerIOThread {
    
    private final WorkerControllerServiceGrpc.WorkerControllerServiceStub controllerServiceStub;
    private final WorkerQueueFactory workerQueueFactory;
    private WorkerTaskResultQueue queue;
    
    @Inject
    public WorkerTaskResultSender(
        final WorkerControllerServiceGrpc.WorkerControllerServiceStub controllerServiceStub,
        final WorkerQueueFactory workerQueueFactory) {
        super(WorkerTaskResultSender.class.getSimpleName());
        this.workerQueueFactory = workerQueueFactory;
        this.controllerServiceStub = controllerServiceStub;
    }
    
    /**
     * {@inheritDoc}
     **/
    @Override
    public synchronized void start(String workerId, String workerGroup) {
        this.queue = new WorkerTaskResultQueue.Default(workerQueueFactory.getOrCreate(workerId, WorkerTaskResult.class));
        super.start(workerId, workerGroup);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doOnLoop() throws Exception {
        WorkerTaskResult result = queue.poll(Duration.ofMillis(Long.MAX_VALUE));
        if (result == null) return;
        
        WorkerJobResultsRequest request = WorkerJobResultsRequest
            .newBuilder()
            .setHeader(newRequestOrResponseHeader())
            .setMessage(MessageFormats.JSON.toByteString(BatchMessage.of(result)))
            .build();
        
        controllerServiceStub.sendWorkerJobResults(request, new StreamObserver<>() {
                @Override
                public void onNext(WorkerJobResultsResponse value) {
                    log.info("onNext {}", value);
                }
                
                @Override
                public void onError(Throwable t) {
                    log.error("Error while sending worker job results", t);
                }
                
                @Override
                public void onCompleted() {
                    log.info("onCompleted");
                }
            }
        );
    }
    
    private RequestOrResponseHeader newRequestOrResponseHeader() {
        return RequestOrResponseHeader
            .newBuilder()
            .setClientId(workerId)
            .setClientVersion(KestraContext.getContext().getVersion())
            .setMessageFormat(MessageFormats.JSON.name())
            .setCorrelationId(UUID.randomUUID().toString())
            .build();
    }
}
