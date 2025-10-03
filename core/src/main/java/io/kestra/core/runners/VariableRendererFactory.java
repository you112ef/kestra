package io.kestra.core.runners;

import io.kestra.core.runners.pebble.functions.SecretFunction;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Factory service for constructing {@link VariableRenderer} instances.
 */
@Singleton
public class VariableRendererFactory {
    
    @Inject
    private ApplicationContext applicationContext;
    
    @Inject
    @Nullable
    private VariableRenderer.VariableConfiguration variableConfiguration;
    
    @Inject
    protected VariableRenderer variableRenderer;
    
    public VariableRenderer getDefault() {
        return variableRenderer;
    }
    
    public VariableRenderer createWithMaskedSecrets() {
        return new VariableRenderer(applicationContext, variableConfiguration, List.of(SecretFunction.NAME));
    }
}
