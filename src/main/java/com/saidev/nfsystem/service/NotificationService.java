package com.saidev.nfsystem.service;

import org.springframework.stereotype.Service;

import com.saidev.nfsystem.dto.NotificationRequest;
import com.saidev.nfsystem.dto.NotificationResponse;
import com.saidev.nfsystem.entity.Notification;
import com.saidev.nfsystem.entity.NotificationStatus;
import com.saidev.nfsystem.entity.User;
import com.saidev.nfsystem.repository.NotificationRepository;
import com.saidev.nfsystem.repository.UserRepository;

import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationProducer notificationProducer;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               NotificationProducer notificationProducer) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationProducer = notificationProducer;
    }

    public NotificationResponse createNotification(NotificationRequest request) {

        //Idempotency check
        var existing = notificationRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            return new NotificationResponse(
                    existing.get().getId(),
                    existing.get().getStatus().name()
            );
        }

        //Fetch user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Create notification
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setIdempotencyKey(request.getIdempotencyKey());

        Notification saved = notificationRepository.save(notification);

        notificationProducer.sendNotificationEvent(saved.getId().toString());
    
        return new NotificationResponse(saved.getId(), saved.getStatus().name());
    }
}