package io.kestra.controller.grpc.server;

import io.grpc.stub.StreamObserver;
import io.kestra.controller.grpc.HeartbeatRequest;
import io.kestra.controller.grpc.HeartbeatResponse;
import io.kestra.controller.grpc.LivenessControllerServiceGrpc;
import io.kestra.controller.messages.HeartbeatMessage;
import io.kestra.controller.messages.HeartbeatMessageReply;
import io.kestra.core.server.ServiceLivenessUpdater;
import io.kestra.core.server.ServiceStateTransition;
import io.kestra.server.internals.MessageFormat;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class LivenessControllerService extends LivenessControllerServiceGrpc.LivenessControllerServiceImplBase {
    
    private final ServiceLivenessUpdater serviceLivenessUpdater;
    
    @Inject
    public LivenessControllerService(ServiceLivenessUpdater serviceLivenessUpdater) {
        this.serviceLivenessUpdater = serviceLivenessUpdater;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        final MessageFormat messageFormat = MessageFormat.resolve(request.getHeader().getMessageFormat());
        
        HeartbeatMessage message = messageFormat
            .fromByteString(request.getMessage(), HeartbeatMessage.class);
        
        ServiceStateTransition.Response response;
        if (message.newState() != null) {
            response = serviceLivenessUpdater.update(message.instance(), message.newState(), message.reason());
        } else {
            serviceLivenessUpdater.update(message.instance());
            response = new ServiceStateTransition.Response(ServiceStateTransition.Result.SUCCEEDED, message.instance());
        }
        
        responseObserver.onNext(HeartbeatResponse
            .newBuilder()
            .setHeader(request.getHeader())
            .setMessage(messageFormat.toByteString(new HeartbeatMessageReply(
                response.instance(),
                response.result()
            )))
            .build()
        );
        responseObserver.onCompleted();
    }
}
