package org.kestra.task.gcp.gcs;

import com.devskiller.friendly_id.FriendlyId;
import com.google.common.collect.ImmutableMap;
import io.micronaut.context.annotation.Value;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.kestra.core.models.tasks.Task;
import org.kestra.core.runners.RunContext;
import org.kestra.core.runners.RunContextFactory;
import org.kestra.core.utils.TestsUtils;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@MicronautTest
class DownloadsTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Value("${kestra.tasks.gcs.bucket}")
    private String bucket;

    @Inject
    private GcsTestUtils testUtils;

    @Value("${kestra.variables.globals.random}")
    private String random;

    @Test
    void run() throws Exception {
        String out1 = FriendlyId.createFriendlyId();
        testUtils.upload(random + "/" + out1);
        String out2 = FriendlyId.createFriendlyId();
        testUtils.upload(random + "/" + out2);

        Downloads task = Downloads.builder()
            .id(DownloadTest.class.getSimpleName())
            .type(Downloads.class.getName())
            .from("gs://" + bucket + "/tasks/gcp/upload/" + random + "/")
            .action(Downloads.Action.DELETE)
            .build();

        Downloads.Output run = task.run(runContext(task));

        assertThat(run.getBlobs().size(), is(2));
    }

    private RunContext runContext(Task task) {
        return TestsUtils.mockRunContext(
            this.runContextFactory,
            task,
            ImmutableMap.of(
                "bucket", this.bucket
            )
        );
    }
}