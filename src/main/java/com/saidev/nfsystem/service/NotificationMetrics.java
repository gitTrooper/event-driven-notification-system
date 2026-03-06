package com.saidev.nfsystem.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class NotificationMetrics {

    private final Counter successCounter;
    private final Counter failureCounter;
    private final Counter retryCounter;
    private final Counter dlqCounter;

    public NotificationMetrics(MeterRegistry registry) {
        successCounter = registry.counter("notifications.success");
        failureCounter = registry.counter("notifications.failure");
        retryCounter = registry.counter("notifications.retry");
        dlqCounter = registry.counter("notifications.dlq");
    }

    public void incrementSuccess() {
        successCounter.increment();
    }

    public void incrementFailure() {
        failureCounter.increment();
    }

    public void incrementRetry() {
        retryCounter.increment();
    }

    public void incrementDLQ() {
        dlqCounter.increment();
    }
}