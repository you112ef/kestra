package io.kestra.core.runners.pebble.functions;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.runners.VariableRenderer;
import io.kestra.core.storages.StorageContext;
import io.kestra.core.storages.StorageInterface;
import io.kestra.core.utils.IdUtils;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@KestraTest
class IsFileEmptyFunctionTest {

    private static final String NAMESPACE = "my.namespace";
    private static final String FLOW = "flow";

    @Inject
    VariableRenderer variableRenderer;

    @Inject
    StorageInterface storageInterface;

    private URI getInternalStorageURI(String executionId) {
        return URI.create("/" + NAMESPACE.replace(".", "/") + "/" + FLOW + "/executions/" + executionId + "/tasks/task/" + IdUtils.create() + "/123456.ion");
    }

    private URI getInternalStorageFile(URI internalStorageURI, String text) throws IOException {
        return storageInterface.put(null, NAMESPACE, internalStorageURI, new ByteArrayInputStream(text.getBytes()));
    }

    @Test
    void shouldReturnFalseForFileWithText() throws IOException, IllegalVariableEvaluationException {
        String executionId = IdUtils.create();
        URI internalStorageURI = getInternalStorageURI(executionId);
        URI internalStorageFile = getInternalStorageFile(internalStorageURI, "NOT AN EMPTY FILE");

        // test for an authorized execution
        Map<String, Object> variables = Map.of(
            "flow", Map.of(
                "id", FLOW,
                "namespace", NAMESPACE),
            "execution", Map.of("id", executionId)
        );
        boolean render = Boolean.parseBoolean(variableRenderer.render("{{ isFileEmpty('" + internalStorageFile + "') }}", variables));
        assertFalse(render);
    }

    @Test
    void readNamespaceFileWithNamespace() throws IllegalVariableEvaluationException, IOException {
        String namespace = "io.kestra.tests";
        String filePath = "file.txt";
        storageInterface.createDirectory(null, namespace, URI.create(StorageContext.namespaceFilePrefix(namespace)));
        storageInterface.put(null, namespace, URI.create(StorageContext.namespaceFilePrefix(namespace) + "/" + filePath), new ByteArrayInputStream("NOT AN EMPTY FILE".getBytes()));

        boolean render = Boolean.parseBoolean(
            variableRenderer.render("{{ isFileEmpty('" + filePath + "', namespace='" + namespace + "') }}",
                Map.of("flow", Map.of("namespace", "flow.namespace"))));
        assertFalse(render);
    }

    @Test
    void findFileNamespaceFileWhitInheritance() throws IllegalVariableEvaluationException, IOException {
        String namespace = "my.parent.namespace";
        String inheritedNamespace = "my.parent";
        String firstLevelNamespace = "my";
        String filePath = "file.txt";
        storageInterface.createDirectory(null, inheritedNamespace, URI.create(StorageContext.namespaceFilePrefix(inheritedNamespace)));
        storageInterface.put(null, inheritedNamespace, URI.create(StorageContext.namespaceFilePrefix(inheritedNamespace) + "/" + filePath), new ByteArrayInputStream("Hello from inherited namespace".getBytes()));

        storageInterface.createDirectory(null, inheritedNamespace, URI.create(StorageContext.namespaceFilePrefix(firstLevelNamespace)));
        storageInterface.put(null, inheritedNamespace, URI.create(StorageContext.namespaceFilePrefix(firstLevelNamespace) + "/" + filePath), new ByteArrayInputStream("".getBytes()));

        boolean render = Boolean.parseBoolean(variableRenderer.render("{{ isFileEmpty('" + filePath + "') }}", Map.of("flow", Map.of("namespace", namespace))));
        assertFalse(render);
    }

    @Test
    void shouldNotFindFileNamespaceFileWhitInheritanceWhenNamespaceSpecified() throws IOException {
        String namespace = "my.specified.namespace";
        String inheritedNamespace = "my.specified";
        String filePath = "file.txt";
        storageInterface.createDirectory(null, inheritedNamespace, URI.create(StorageContext.namespaceFilePrefix(inheritedNamespace)));
        storageInterface.put(null, inheritedNamespace, URI.create(StorageContext.namespaceFilePrefix(inheritedNamespace) + "/" + filePath), new ByteArrayInputStream("Hello from inherited specified namespace".getBytes()));

        assertThrows(IllegalVariableEvaluationException.class, () -> variableRenderer.render("{{ isFileEmpty('" + filePath + "', namespace='" + namespace + "') }}", Map.of("flow", Map.of("namespace", "my.current.namespace"))));
    }

    @Test
    void shouldReturnTrueForEmpty() throws IOException, IllegalVariableEvaluationException {
        String executionId = IdUtils.create();
        URI internalStorageURI = getInternalStorageURI(executionId);
        URI internalStorageFile = getInternalStorageFile(internalStorageURI, "");

        // test for an authorized execution
        Map<String, Object> variables = Map.of(
            "flow", Map.of(
                "id", FLOW,
                "namespace", NAMESPACE),
            "execution", Map.of("id", executionId)
        );
        boolean render = Boolean.parseBoolean(variableRenderer.render("{{ isFileEmpty('" + internalStorageFile + "') }}", variables));
        assertTrue(render);
    }
}