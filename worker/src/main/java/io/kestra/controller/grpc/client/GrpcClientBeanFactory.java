package io.kestra.controller.grpc.client;

import io.kestra.controller.grpc.LivenessControllerServiceGrpc;
import io.kestra.server.GrpcChannelProvider;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
public class GrpcClientBeanFactory {
    
    @Bean
    @Singleton
    public LivenessControllerServiceGrpc.LivenessControllerServiceBlockingStub workerServiceStub(GrpcChannelProvider grpcChannelProvider) {
        return LivenessControllerServiceGrpc.newBlockingStub(grpcChannelProvider.createOrGetDefault());
    }
    
}
