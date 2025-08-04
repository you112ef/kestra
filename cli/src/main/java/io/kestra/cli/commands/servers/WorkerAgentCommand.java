package io.kestra.cli.commands.servers;

import com.google.common.collect.ImmutableMap;
import io.kestra.core.contexts.KestraContext;
import io.kestra.core.models.ServerType;
import io.kestra.core.utils.Await;
import io.kestra.worker.Worker;
import io.micronaut.context.ApplicationContext;
import jakarta.inject.Inject;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.Map;

@CommandLine.Command(
    name = "worker-agent",
    description = "Start the Kestra worker"
)
public class WorkerAgentCommand extends AbstractServerCommand {

    @Inject
    private ApplicationContext applicationContext;

    @Option(names = {"-t", "--thread"}, description = "The max number of worker threads, defaults to four times the number of available processors")
    private int thread = defaultWorkerThread();

    @Option(names = {"-g", "--worker-group"}, description = "The worker group key, must match the regex [a-zA-Z0-9_-]+ (EE only)")
    private String workerGroupKey = null;

    @SuppressWarnings("unused")
    public static Map<String, Object> propertiesOverrides() {
        return ImmutableMap.of(
            "kestra.server-type", ServerType.WORKER_AGENT
        );
    }

    @Override
    public Integer call() throws Exception {

        KestraContext.getContext().injectWorkerConfigs(thread, workerGroupKey);

        super.call();

        if (this.workerGroupKey != null && !this.workerGroupKey.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("The --worker-group option must match the [a-zA-Z0-9_-]+ pattern");
        }
        
        Worker worker = applicationContext.getBean(Worker.class);
        worker.start(thread, workerGroupKey);

        Await.until(() -> !this.applicationContext.isRunning());

        return 0;
    }

    public String workerGroupKey() {
        return workerGroupKey;
    }
}
