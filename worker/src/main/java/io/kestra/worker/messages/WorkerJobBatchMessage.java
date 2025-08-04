package io.kestra.worker.messages;

import io.kestra.core.runners.WorkerJob;

import java.util.List;
import java.util.Optional;

public record WorkerJobBatchMessage(
    List<WorkerJob> jobs
) {
    
    public List<WorkerJob> getJobs() {
        return Optional.ofNullable(jobs).orElse(List.of());
    }
}
