package io.kestra.controller.grpc.server;

import com.fasterxml.jackson.core.type.TypeReference;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.kestra.core.queues.QueueException;
import io.kestra.core.queues.QueueFactoryInterface;
import io.kestra.core.queues.QueueInterface;
import io.kestra.core.queues.WorkerJobQueueInterface;
import io.kestra.core.runners.Worker;
import io.kestra.core.runners.WorkerInstance;
import io.kestra.core.runners.WorkerJob;
import io.kestra.core.runners.WorkerJobRunningStateStore;
import io.kestra.core.runners.WorkerTask;
import io.kestra.core.runners.WorkerTaskResult;
import io.kestra.core.runners.WorkerTaskRunning;
import io.kestra.core.runners.WorkerTrigger;
import io.kestra.core.runners.WorkerTriggerRunning;
import io.kestra.server.internals.BatchMessage;
import io.kestra.server.internals.MessageFormat;
import io.kestra.worker.grpc.FetchWorkerJobRequest;
import io.kestra.worker.grpc.FetchWorkerJobResponse;
import io.kestra.worker.grpc.WorkerControllerServiceGrpc;
import io.kestra.worker.grpc.WorkerJobResultsRequest;
import io.kestra.worker.grpc.WorkerJobResultsResponse;
import io.kestra.worker.messages.FetchWorkerJobMessage;
import io.kestra.worker.messages.WorkerJobBatchMessage;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Slf4j
public class WorkerControllerService extends WorkerControllerServiceGrpc.WorkerControllerServiceImplBase {
    
    public static final TypeReference<BatchMessage<WorkerTaskResult>> WORKER_TASK_RESULT_BATCH_MESSAGE_TYPE_REFERENCE = new TypeReference<>() {
    };
    
    @Inject
    @Named(QueueFactoryInterface.WORKERJOB_NAMED)
    private WorkerJobQueueInterface workerJobQueue;
    
    @Inject
    @Named(QueueFactoryInterface.WORKERTASKRESULT_NAMED)
    private QueueInterface<WorkerTaskResult> workerTaskResultQueue;
    
    @Inject
    private WorkerJobRunningStateStore workerJobRunningStateStore;
    
    private final ConcurrentHashMap<String, Runnable> disposables = new ConcurrentHashMap<>();
    
    @Override
    public void fetchWorkerJobsStream(FetchWorkerJobRequest request, StreamObserver<FetchWorkerJobResponse> responseObserver) {
        final MessageFormat messageFormat = MessageFormat.resolve(request.getHeader().getMessageFormat());
        
        FetchWorkerJobMessage message = messageFormat.fromByteString(request.getMessage(), FetchWorkerJobMessage.class);
        
        
        ServerCallStreamObserver<FetchWorkerJobResponse> serverObserver = (ServerCallStreamObserver<FetchWorkerJobResponse>) responseObserver;
        
        log.info("Received worker-job request from worker [{}]", message.workerId());
        serverObserver.setOnCancelHandler(() -> {
            log.info("Worker [{}] disconnected or cancelled", message.workerId());
            Optional.ofNullable(disposables.remove(message.workerId())).ifPresent(Runnable::run);
        });
        
        // TODO 
        //  Currently consumer thread is managed directly by the WorkerJobQueue.
        //  It could be preferable that the WorkerControllerServer start a polling thread 
        //  for consuming the workerJobQueue (e.g., via a poll method) to be able to manage it more properly on cancel. 
        Runnable stopReceiving = this.workerJobQueue.receive(message.workerGroup(), Worker.class, either -> {
            if (either.isRight()) {
                log.error("Unable to deserialize a worker job: {}", either.getRight().getMessage());
                return;
            }
            WorkerJob job = either.getLeft();
            log.info("Sending job [{}] to worker [{}]", job.uid(), message.workerId()); // TODO change to debug
            serverObserver.onNext(FetchWorkerJobResponse
                .newBuilder()
                .setHeader(request.getHeader())
                .setMessage(messageFormat.toByteString(new WorkerJobBatchMessage(List.of(job))))
                .build()
            );
            
            WorkerInstance workerInstance = new WorkerInstance(message.workerId(), message.workerGroup());
            if (job instanceof WorkerTask workerTask) {
                workerJobRunningStateStore.put(WorkerTaskRunning.of(workerTask, workerInstance, -1));
            } else if (job instanceof WorkerTrigger workerTrigger) {
                workerJobRunningStateStore.put(WorkerTriggerRunning.of(workerTrigger, workerInstance, -1));
            } else {
                log.error("Message is of type [{}] which should never occurs", job);
            }
            
        }, false);
        disposables.put(message.workerId(), () -> {
            stopReceiving.run();
            serverObserver.onCompleted();
        });
    }
    
    @Override
    public void sendWorkerJobResults(WorkerJobResultsRequest request, StreamObserver<WorkerJobResultsResponse> responseObserver) {
        final MessageFormat messageFormat = MessageFormat.resolve(request.getHeader().getMessageFormat());
        BatchMessage<WorkerTaskResult> message = messageFormat.fromByteString(request.getMessage(), WORKER_TASK_RESULT_BATCH_MESSAGE_TYPE_REFERENCE);
        message.records().forEach(workerTaskResult -> {
            try {
                workerTaskResultQueue.emit(workerTaskResult);
            } catch (QueueException e) {
                throw new RuntimeException(e);
            }
        });
        responseObserver.onNext(WorkerJobResultsResponse
            .newBuilder()
            .setHeader(request.getHeader())
            .build()
        );
        responseObserver.onCompleted();
    }
    
    @PreDestroy
    public void close() {
        this.disposables.values().forEach(Runnable::run);
    }
}
