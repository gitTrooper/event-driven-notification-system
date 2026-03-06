package com.saidev.nfsystem.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class RetryScheduler {

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(2);

    public void schedule(Runnable task, long delaySeconds) {
        scheduler.schedule(task, delaySeconds, TimeUnit.SECONDS);
    }
}