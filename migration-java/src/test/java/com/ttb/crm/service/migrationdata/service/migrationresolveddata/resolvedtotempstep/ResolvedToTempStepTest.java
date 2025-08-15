package com.ttb.crm.service.migrationdata.service.migrationresolveddata.resolvedtotempstep;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ResolvedToTempStepTest {

    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;
    private ResolvedToTempStep resolvedToTempStep;

    @BeforeEach
    void setUp() {
        jobRepository = mock(JobRepository.class);
        transactionManager = mock(PlatformTransactionManager.class);
        resolvedToTempStep = new ResolvedToTempStep(jobRepository, transactionManager);
    }

    @Test
    void testCompletedToTempStepCreation() {
        RepositoryItemReader<StgCaseInProgressModel> reader = mock(RepositoryItemReader.class);
        ItemWriter<TempStgCaseInProgressLogModel> writer = mock(ItemWriter.class);
        ResolvedToTempProcessor processor = mock(ResolvedToTempProcessor.class);

        Step step = resolvedToTempStep.CompletedToTemp(reader, writer, processor);

        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("resolvedToTemp");
    }
}
