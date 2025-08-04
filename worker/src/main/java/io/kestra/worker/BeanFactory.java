package io.kestra.worker;

import io.kestra.controller.GrpcServiceLivenessUpdater;
import io.kestra.controller.grpc.LivenessControllerServiceGrpc;
import io.kestra.core.server.ServiceLivenessUpdater;
import io.kestra.server.GrpcChannelProvider;
import io.kestra.worker.grpc.WorkerControllerServiceGrpc;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;

@Factory
public class BeanFactory {
    
    @Singleton
    @Primary
    public ServiceLivenessUpdater serviceLivenessUpdater(LivenessControllerServiceGrpc.LivenessControllerServiceBlockingStub client) {
        return new GrpcServiceLivenessUpdater(client);
    }
    
    @Bean
    @Singleton
    public WorkerControllerServiceGrpc.WorkerControllerServiceBlockingStub blockingWorkerServiceStub(GrpcChannelProvider grpcChannelProvider) {
        return WorkerControllerServiceGrpc.newBlockingStub(grpcChannelProvider.createOrGetDefault());
    }
    
    @Bean
    @Singleton
    public WorkerControllerServiceGrpc.WorkerControllerServiceStub asyncWorkerServiceStub(GrpcChannelProvider grpcChannelProvider) {
        return WorkerControllerServiceGrpc.newStub(grpcChannelProvider.createOrGetDefault());
    }
}
