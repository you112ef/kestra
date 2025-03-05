package io.kestra.core.test.flow;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InputFixture {
    @NotNull
    private String id;

    @NotNull
    private String value;
}
