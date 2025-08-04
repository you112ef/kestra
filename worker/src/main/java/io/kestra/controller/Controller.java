package io.kestra.controller;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.kestra.controller.grpc.server.LivenessControllerService;
import io.kestra.controller.grpc.server.WorkerControllerService;
import io.kestra.core.server.Service;
import io.kestra.core.server.ServiceStateChangeEvent;
import io.kestra.core.server.ServiceType;
import io.kestra.server.AbstractService;
import io.micronaut.context.event.ApplicationEventPublisher;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Singleton
public class Controller extends AbstractService implements Service {
    
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    
    private Server server;
    
    private final WorkerControllerService workerControllerService;
    private final LivenessControllerService livenessControllerService;
    
    @Inject
    public Controller(
        WorkerControllerService workerControllerService,
        LivenessControllerService livenessControllerService,
        ApplicationEventPublisher<ServiceStateChangeEvent> eventPublisher) {
        super(ServiceType.CONTROLLER, eventPublisher);
        this.workerControllerService = workerControllerService;
        this.livenessControllerService = livenessControllerService;
    }
    
    @PostConstruct
    public void start() throws IOException {
        if (getState() != ServiceState.CREATED) {
            throw new IllegalStateException("Controller is already started or stopped");
        }
        
        log.info("Starting Controller");
        /* The port on which the server should run */
        int port = 9096; // TODO to externalize
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
            .addService(workerControllerService)
            .addService(livenessControllerService)
            .build()
            .start();
        log.info("Controller started, listening on {}", port);
        setState(ServiceState.RUNNING);
    }

    @Override
    protected ServiceState doStop() throws InterruptedException  {
        if (server != null && !server.isTerminated()) {
            shutdownServerAndWait();
        }
        return ServiceState.TERMINATED_GRACEFULLY;
    }
    
    private void shutdownServerAndWait() throws InterruptedException {
        server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
}
