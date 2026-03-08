package com.saidev.nfsystem.controller;

import com.saidev.nfsystem.dto.NotificationRequest;
import com.saidev.nfsystem.dto.NotificationResponse;
import com.saidev.nfsystem.service.KafkaLagMonitor;
import com.saidev.nfsystem.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final KafkaLagMonitor lagMonitor;

    public NotificationController(NotificationService notificationService,
                                KafkaLagMonitor lagMonitor) {
        this.notificationService = notificationService;
        this.lagMonitor = lagMonitor;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request) {

            if (lagMonitor.getLagValue() > 1000) {
                return ResponseEntity
                    .status(429)
                    .body(new NotificationResponse(null, "SYSTEM_OVERLOADED"));
            }

        NotificationResponse response =
                notificationService.createNotification(request);

        return ResponseEntity.ok(response);
    }
}
