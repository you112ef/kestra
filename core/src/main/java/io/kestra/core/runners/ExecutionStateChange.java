package io.kestra.core.runners;

import io.kestra.core.models.HasUID;
import io.kestra.core.models.executions.Execution;
import io.kestra.core.models.flows.State;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder
public class ExecutionStateChange implements HasUID {
    @NotNull
    Execution execution;

    @NotNull
    State.Type oldState;

    @NotNull
    State.Type newState;

    @Override
    public String uid() {
        return execution.getId();
    }
}
