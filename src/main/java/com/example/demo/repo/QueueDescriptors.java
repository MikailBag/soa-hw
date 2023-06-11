package com.example.demo.repo;

import com.example.demo.api.chat.Chat;
import com.example.demo.report.ReportTask;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class QueueDescriptors {
    private QueueDescriptors() {
    }

    public static final QueueDescriptor<Chat.ServerInternalMessageDto> CHAT_MESSAGES = new QueueDescriptor<>() {
        @Override
        public String name() {
            return "chat_messages";
        }

        @Override
        public Policy policy() {
            return Policy.PUB_SUB;
        }

        @Override
        public byte[] serialize(Chat.ServerInternalMessageDto value) {
            return value.toByteArray();
        }

        @Override
        public Chat.ServerInternalMessageDto deserialize(byte[] data) throws IOException {
            return Chat.ServerInternalMessageDto.parseFrom(data);
        }
    };

    public static final QueueDescriptor<ReportTask> REPORT_TASKS = new QueueDescriptor<>() {
        private static final ObjectMapper MAPPER = new ObjectMapper();
        @Override
        public String name() {
            return "report_tasks";
        }

        @Override
        public Policy policy() {
            return Policy.WORK_QUEUE;
        }

        @Override
        public byte[] serialize(ReportTask value) throws IOException {
            return MAPPER.writeValueAsBytes(value);
        }

        @Override
        public ReportTask deserialize(byte[] data) throws IOException {
            return MAPPER.readValue(data, ReportTask.class);
        }
    };
}
