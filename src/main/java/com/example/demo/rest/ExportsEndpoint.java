package com.example.demo.rest;

import com.example.demo.repo.MessageBroker;
import com.example.demo.repo.ObjectStorage;
import com.example.demo.repo.QueueDescriptors;
import com.example.demo.report.ReportTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/exports")
public class ExportsEndpoint {
    private final MessageBroker messageBroker;
    private final ObjectStorage objectStorage;
    @Autowired
    ExportsEndpoint(
            MessageBroker messageBroker,
            ObjectStorage objectStorage
    ) {
        this.messageBroker = messageBroker;
        this.objectStorage = objectStorage;
    }

    private record CreateRequest(
            String username
    ) {}

    private record CreateResponse(
            String exportId
    ) {}

    @PostMapping
    CreateResponse create(@RequestBody CreateRequest request) throws IOException {
        String id = UUID.randomUUID().toString();
        messageBroker.publish(QueueDescriptors.REPORT_TASKS, new ReportTask(id, request.username()));
        return new CreateResponse(id);
    }

    @GetMapping("/{id}")
    byte[] get(@PathVariable("id") String exportId) {
        byte[] val = objectStorage.get(exportId);
        return Objects.requireNonNullElseGet(val, () -> new byte[0]);
    }
}
