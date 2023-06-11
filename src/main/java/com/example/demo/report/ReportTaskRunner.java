package com.example.demo.report;

import com.example.demo.repo.MessageBroker;
import com.example.demo.repo.ObjectStorage;
import com.example.demo.repo.QueueDescriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

@Component
public class ReportTaskRunner implements AutoCloseable, Consumer<ReportTask> {
    private final Logger log = LoggerFactory.getLogger(ReportTaskRunner.class);

    private final ReportGenerator generator;
    private final ExecutorService executor;
    private final MessageBroker.Subscription subscription;
    private final ObjectStorage objectStorage;
    private final Semaphore semaphore = new Semaphore(4);

    @Autowired
    ReportTaskRunner(
            ReportGenerator generator,
            MessageBroker messageBroker,
            ObjectStorage objectStorage
    ) {
        this.generator = generator;
        this.executor = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual()
                        .name("ReportTask-", 0)
                        .factory()
        );
        this.objectStorage = objectStorage;
        this.subscription = messageBroker.subscribe(QueueDescriptors.REPORT_TASKS, this);
    }

    @Override
    public void close() throws Exception {
        log.info("Stopping subscription");
        this.subscription.unsubscribe();
        log.info("Closing executor");
        this.executor.close();
        log.info("Closed");
    }

    @Override
    public void accept(ReportTask task) {
        try {
            semaphore.acquire();
        } catch (InterruptedException ex) {
            log.warn("interrupted while waiting permit");
            Thread.currentThread().interrupt();
        }
        log.info("Starting task");
        executor.execute(() -> {
            try {
                byte[] pdf = generator.generate(task.username());
                objectStorage.put(task.id(), pdf);
            } catch (Exception ex) {
                log.error("task failed", ex);
            } finally {
                semaphore.release();
            }
        });
    }
}
