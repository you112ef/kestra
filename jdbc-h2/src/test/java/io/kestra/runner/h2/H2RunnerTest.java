package io.kestra.runner.h2;

import io.kestra.core.junit.annotations.LoadFlows;
import io.kestra.jdbc.runner.JdbcRunnerTest;
import org.junit.jupiter.api.Test;

public class H2RunnerTest extends JdbcRunnerTest {

    @Test
    @LoadFlows({"flows/valids/restart-with-after-execution.yaml"})
    protected void restartFailedWitertghAfterExecution() throws Exception {
        restartCaseTest.restartFailedWithAfterExecution();
    }
}
