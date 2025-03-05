package io.kestra.core.test.flow;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Fixtures {
    private List<InputFixture> inputs;

    private List<TaskFixture> tasks;

    private TriggerFixture trigger;
}
