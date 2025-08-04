package io.kestra.controller;

import io.kestra.controller.grpc.HeartbeatRequest;
import io.kestra.controller.grpc.HeartbeatResponse;
import io.kestra.controller.grpc.LivenessControllerServiceGrpc.LivenessControllerServiceBlockingStub;
import io.kestra.controller.messages.HeartbeatMessage;
import io.kestra.controller.messages.HeartbeatMessageReply;
import io.kestra.core.contexts.KestraContext;
import io.kestra.core.server.Service;
import io.kestra.core.server.ServiceInstance;
import io.kestra.core.server.ServiceLivenessUpdater;
import io.kestra.core.server.ServiceStateTransition;
import io.kestra.server.grpc.RequestOrResponseHeader;
import io.kestra.server.internals.MessageFormat;
import io.kestra.server.internals.MessageFormats;
import io.micronaut.context.annotation.Secondary;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Objects;
import java.util.UUID;

public class GrpcServiceLivenessUpdater implements ServiceLivenessUpdater {
    
    private final LivenessControllerServiceBlockingStub client;
    
    public GrpcServiceLivenessUpdater(final LivenessControllerServiceBlockingStub client) {
        this.client = Objects.requireNonNull(client, "client must not be null.");
    }
    
    /** {@inheritDoc} **/
    @Override
    public void update(ServiceInstance service) {
        update(service, null, null);
    }
    
    /** {@inheritDoc} **/
    @Override
    public ServiceStateTransition.Response update(ServiceInstance instance, Service.ServiceState newState, String reason) {
        HeartbeatResponse response = client.heartbeat(HeartbeatRequest
            .newBuilder()
            .setHeader(RequestOrResponseHeader
                .newBuilder()
                .setClientId(instance.uid())
                .setClientVersion(KestraContext.getContext().getVersion())
                .setMessageFormat(MessageFormats.JSON.name())
                .setCorrelationId(UUID.randomUUID().toString())
                .build()
            )
            .setMessage(MessageFormats.JSON.toByteString(new HeartbeatMessage(instance, newState, reason)))
            .build()
        );
        
        HeartbeatMessageReply messageReply = MessageFormat
            .resolve(response.getHeader().getMessageFormat())
            .fromByteString(response.getMessage(), HeartbeatMessageReply.class);
        
        return new ServiceStateTransition.Response(messageReply.result(), messageReply.instance());
    }
}
