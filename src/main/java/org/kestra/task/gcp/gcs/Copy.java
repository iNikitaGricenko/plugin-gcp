package org.kestra.task.gcp.gcs;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.common.collect.ImmutableMap;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.kestra.core.models.executions.metrics.Counter;
import org.kestra.core.models.tasks.RunnableTask;
import org.kestra.core.models.tasks.Task;
import org.kestra.core.runners.RunContext;
import org.kestra.core.runners.RunOutput;
import org.slf4j.Logger;

import java.net.URI;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public class Copy extends Task implements RunnableTask {
    private String from;
    private String to;
    private String projectId;

    @Builder.Default
    private boolean delete = false;

    @Override
    public RunOutput run(RunContext runContext) throws Exception {
        Storage connection = new Connection().of(runContext.render(this.projectId));
        Logger logger = runContext.logger(this.getClass());
        URI from = new URI(runContext.render(this.from));
        URI to = new URI(runContext.render(this.to));

        BlobId source = BlobId.of(from.getScheme().equals("gs") ? from.getAuthority() : from.getScheme(), from.getPath().substring(1));

        logger.debug("Moving from '{}' to '{}'", from, to);

        Blob result = connection
            .copy(Storage.CopyRequest.newBuilder()
                .setSource(source)
                .setTarget(BlobId.of(to.getAuthority(), to.getPath().substring(1)))
                .build()
            )
            .getResult();

        runContext.metric(Counter.of("file.size", result.getSize()));

        if (this.delete) {
            connection.delete(source);
        }

        return RunOutput
            .builder()
            .outputs(ImmutableMap.of("uri", new URI("gs://" + result.getBucket() + "/" + result.getName())))
            .build();
    }
}
