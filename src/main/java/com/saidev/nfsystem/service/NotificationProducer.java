package com.saidev.nfsystem.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public NotificationProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotificationEvent(String message) {
        kafkaTemplate.send("notification-events", message);
    }

    public void sendRetry5s(String message) {
        kafkaTemplate.send("notification-events-retry-5s", message);
    }
    
    public void sendRetry30s(String message) {
        kafkaTemplate.send("notification-events-retry-30s", message);
    }

    public void sendToDLQ(String message) {
        kafkaTemplate.send("notification-events-dlq", message);
    }
}

