package io.kestra.server;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.kestra.core.contexts.KestraContext;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
@Slf4j
public class GrpcChannelProvider {
    
    private volatile ManagedChannel defaultChannel;
    private volatile ExecutorService defaultExecutorService;
    
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    
    /**
     * Return a shared gRPC Channel. 
     * <p>
     * This method will create the channel if necessary.
     * 
     * @return the {@link Channel}
     */
    public Channel createOrGetDefault() {
        // TODO externalize all config
        if (this.defaultChannel == null) {
            synchronized (this) {
                if (this.defaultChannel == null) {
                    defaultExecutorService = Executors.newSingleThreadExecutor();
                    defaultChannel = ManagedChannelBuilder.forAddress("localhost", 9096)
                        .usePlaintext()
                        .enableRetry()
                        .maxRetryAttempts(10)
                        .userAgent(getUserAgent())
                        .keepAliveTime(1, TimeUnit.HOURS)
                        .keepAliveWithoutCalls(true)
                        .executor(defaultExecutorService)
                        .build();
                }
            }
        }
        return defaultChannel;
    }
    
    @PreDestroy
    public void close() {
        if (!stopped.compareAndSet(false, true)) {
            return; // Method called twice
        }
        
        if (this.defaultChannel != null && !this.defaultChannel.isShutdown()) {
            try {
                shutdownServerAndWait();
            } catch (Exception e) {
                log.debug("Error while stopping default gRPC channel", e);
                if (e instanceof InterruptedException)
                    Thread.currentThread().interrupt();
            }
            this.defaultExecutorService.shutdownNow();
        }
    }
    
    private void shutdownServerAndWait() throws InterruptedException {
        this.defaultChannel.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
    
    private static String getUserAgent() {
        return "Kestra/" + KestraContext.getContext().getVersion();
    }
}
