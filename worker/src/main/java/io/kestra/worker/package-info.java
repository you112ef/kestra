@Configuration
@Requires(property = "kestra.server-type", pattern = "WORKER_AGENT")
package io.kestra.worker;

import io.micronaut.context.annotation.Configuration;
import io.micronaut.context.annotation.Requires;