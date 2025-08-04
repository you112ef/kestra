package io.kestra.worker.messages;

public record FetchWorkerJobMessage(
    String workerId,
    String workerGroup
) {
}
