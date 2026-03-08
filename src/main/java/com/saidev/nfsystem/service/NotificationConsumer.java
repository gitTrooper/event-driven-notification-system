package com.saidev.nfsystem.service;

import com.saidev.nfsystem.entity.Notification;
import com.saidev.nfsystem.entity.NotificationStatus;
import com.saidev.nfsystem.repository.NotificationRepository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final NotificationProducer notificationProducer;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationMetrics metrics;

    public NotificationConsumer(NotificationRepository notificationRepository,
                                NotificationProducer notificationProducer,
                                RedisTemplate<String, String> redisTemplate,
                                NotificationMetrics metrics) {
        this.notificationRepository = notificationRepository;
        this.notificationProducer = notificationProducer;
        this.redisTemplate = redisTemplate;
        this.metrics = metrics;
        System.out.println("Consumer initialized");
    }

    @Transactional
    @KafkaListener(topics = {"notification-events"})
    public void consume(String message) {

        System.out.println("Received notification event: " + message);

        UUID notificationId = UUID.fromString(message);

        //Atomic claim FIRST — prevents concurrent double-processing at DB level
        int updated = notificationRepository.markAsProcessing(notificationId);

        if (updated == 0) {
            System.out.println("Already processing or not in PENDING state, skipping");
            return;
        }

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow();

        //Redis dedupe — scoped per attempt so retries are NOT blocked
        String dedupeKey = "notif:" + notificationId + ":attempt:" + notification.getRetryCount();

        Boolean inserted = redisTemplate.opsForValue()
                .setIfAbsent(dedupeKey, "processed", Duration.ofHours(24));

        if (Boolean.FALSE.equals(inserted)) {
            System.out.println("Duplicate message detected for attempt " + notification.getRetryCount() + ", skipping");
            return;
        }

        try {

            //Redis rate limiting per user
            String rateKey = "user:" + notification.getUser().getId() + ":rate";

            Long count = redisTemplate.opsForValue().increment(rateKey, 1);

            if (count == 1) {
                redisTemplate.expire(rateKey, Duration.ofMinutes(1));
            }

            if (count > 10) {
                System.out.println("Rate limit exceeded for user: " + notification.getUser().getId());
                // Treat as a retryable failure — fall through to catch by re-throwing
                throw new RuntimeException("Rate limit exceeded");
            }

            // 4️⃣ Simulate sending notification
            System.out.println("Sending notification to user: " + notification.getUser().getId());

            //Success case
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            System.out.println("Notification sent successfully");
            metrics.incrementSuccess();


        } catch (Exception e) {

            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setLastError(e.getMessage());

            int retryCount = notification.getRetryCount();
            metrics.incrementRetry();

            if (retryCount == 1) {

                notification.setStatus(NotificationStatus.PENDING);
                notificationRepository.save(notification);

                System.out.println("Retry attempt 1 → sending to retry-5s topic");
                notificationProducer.sendRetry5s(notificationId.toString());

            } else if (retryCount == 2) {

                notification.setStatus(NotificationStatus.PENDING);
                notificationRepository.save(notification);

                System.out.println("Retry attempt 2 → sending to retry-30s topic");
                notificationProducer.sendRetry30s(notificationId.toString());

            } else {

                notification.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(notification);

                System.out.println("Max retries reached → sending to DLQ");
                notificationProducer.sendToDLQ(notificationId.toString());
                metrics.incrementFailure();
                metrics.incrementDLQ();
            }
        }
    }

    @KafkaListener(topics = "notification-events-retry-5s")
    public void retry5s(String message) throws InterruptedException {
        Thread.sleep(5000);
        notificationProducer.sendNotificationEvent(message);
    }

    @KafkaListener(topics = "notification-events-retry-30s")
    public void retry30s(String message) throws InterruptedException {
        Thread.sleep(30000);
        notificationProducer.sendNotificationEvent(message);
    }
}