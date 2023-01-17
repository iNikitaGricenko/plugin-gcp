package io.kestra.plugin.gcp.gcs;

import com.devskiller.friendly_id.FriendlyId;
import com.google.common.collect.ImmutableMap;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.storages.StorageInterface;
import io.kestra.core.utils.TestsUtils;
import io.kestra.plugin.gcp.gcs.models.Blob;
import io.micronaut.context.annotation.Value;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Objects;
import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@MicronautTest
class DeleteListTest {
    @Inject
    private StorageInterface storageInterface;

    @Inject
    private RunContextFactory runContextFactory;

    @Value("${kestra.tasks.gcs.bucket}")
    private String bucket;

    @Test
    void run() throws Exception {
        String dir = FriendlyId.createFriendlyId();

        for (int i = 0; i < 10; i++) {
            ListTest.upload(storageInterface, bucket, runContextFactory, "/tasks/gcp/" + dir);
        }

        DeleteList task = DeleteList.builder()
            .id(DeleteList.class.getSimpleName())
            .type(DeleteList.class.getName())
            .concurrent(8)
            .from("gs://" + this.bucket + "/tasks/gcp/" + dir + "/")
            .build();
        DeleteList.Output run = task.run(TestsUtils.mockRunContext(this.runContextFactory, task, ImmutableMap.of()));

        assertThat(run.getCount(), is(10L));
        assertThat(run.getSize(), greaterThan(6000L));
    }
}
