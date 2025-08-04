package io.kestra.worker;

import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.kestra.core.contexts.KestraContext;
import io.kestra.core.runners.WorkerJob;
import io.kestra.server.grpc.RequestOrResponseHeader;
import io.kestra.server.internals.MessageFormat;
import io.kestra.server.internals.MessageFormats;
import io.kestra.worker.grpc.FetchWorkerJobRequest;
import io.kestra.worker.grpc.FetchWorkerJobResponse;
import io.kestra.worker.grpc.WorkerControllerServiceGrpc.WorkerControllerServiceStub;
import io.kestra.worker.messages.FetchWorkerJobMessage;
import io.kestra.worker.messages.WorkerJobBatchMessage;
import io.kestra.worker.queues.WorkerJobQueue;
import io.kestra.worker.queues.WorkerQueueFactory;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Component responsible for fetching worker jobs.
 */
@Prototype
@Slf4j
public class WorkerJobFetcher extends WorkerIOThread {
    
    private final WorkerControllerServiceStub workerControllerServiceStub;
    private final WorkerQueueFactory workerQueueFactory;
    private WorkerJobQueue workerJobQueue;
    
    private final AtomicReference<ClientCallStreamObserver<FetchWorkerJobRequest>> currentStreamObserver = new AtomicReference<>();
    
    @Inject
    public WorkerJobFetcher(
        final WorkerControllerServiceStub workerControllerServiceStub,
        final WorkerQueueFactory workerQueueFactory) {
        super(WorkerJobFetcher.class.getSimpleName());
        this.workerQueueFactory = workerQueueFactory;
        this.workerControllerServiceStub = workerControllerServiceStub;
    }
    
    /**
     * {@inheritDoc}
     **/
    @Override
    public synchronized void start(String workerId, String workerGroup) {
        this.workerJobQueue = new WorkerJobQueue.Default(workerQueueFactory.getOrCreate(workerId, WorkerJob.class));
        super.start(workerId, workerGroup);
    }
    
    /**
     * {@inheritDoc}
     **/
    @Override
    protected void doOnLoop() throws Exception {
        
        FetchWorkerJobRequest request = FetchWorkerJobRequest.newBuilder()
            .setHeader(newRequestOrResponseHeader())
            .setMessage(MessageFormats.JSON.toByteString(new FetchWorkerJobMessage(workerId, workerGroup)))
            .build();
        CountDownLatch completed = new CountDownLatch(1);
        
        // Start the streaming call
        ClientResponseObserver<FetchWorkerJobRequest, FetchWorkerJobResponse> streamCompleted = new ClientResponseObserver<>() {
            
            @Override
            public void beforeStart(ClientCallStreamObserver<FetchWorkerJobRequest> requestStream) {
                currentStreamObserver.set(requestStream);
            }
            
            @Override
            public void onNext(FetchWorkerJobResponse response) {
                log.info("Stream onNext: {}", response);
                String messageFormat = response.getHeader().getMessageFormat();
                WorkerJobBatchMessage workerJobBatch = MessageFormat
                    .resolve(messageFormat)
                    .fromByteString(response.getMessage(), WorkerJobBatchMessage.class);
                
                if (workerJobBatch != null && !workerJobBatch.jobs().isEmpty()) {
                    workerJobBatch.jobs().forEach(workerJobQueue::put);
                }
            }
            
            @Override
            public void onError(Throwable t) {
                log.error("Stream error: {}", t.getMessage(), t);
                completed.countDown();
            }
            
            @Override
            public void onCompleted() {
                log.error("Stream completed");
                completed.countDown();
            }
        };
        workerControllerServiceStub.fetchWorkerJobsStream(request, streamCompleted);
        completed.await(); // Block until stream ends
    }
    
    @Override
    protected void doOnStop() {
        ClientCallStreamObserver<FetchWorkerJobRequest> active = currentStreamObserver.getAndSet(null);
        if (active != null) {
            active.cancel("Worker stopping", null);
        }
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
