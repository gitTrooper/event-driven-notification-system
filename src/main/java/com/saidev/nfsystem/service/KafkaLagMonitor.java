package com.saidev.nfsystem.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KafkaLagMonitor {

    private final AdminClient adminClient;
    private final MeterRegistry meterRegistry;

    private double lagValue = 0;

    public KafkaLagMonitor(AdminClient adminClient, MeterRegistry meterRegistry) {
        this.adminClient = adminClient;
        this.meterRegistry = meterRegistry;

        Gauge.builder("kafka.consumer.lag", () -> lagValue)
                .register(meterRegistry);
    }

    @Scheduled(fixedDelay = 5000)
    public void monitorLag() throws Exception {

        String topic = "notification-events";
        String groupId = "notification-group";

        ListConsumerGroupOffsetsResult offsets =
                adminClient.listConsumerGroupOffsets(groupId);

        Map<TopicPartition, OffsetAndMetadata> consumerOffsets =
                offsets.partitionsToOffsetAndMetadata().get();

        Map<TopicPartition, OffsetSpec> requestLatestOffsets = new HashMap<>();

        for (TopicPartition tp : consumerOffsets.keySet()) {
            requestLatestOffsets.put(tp, OffsetSpec.latest());
        }

        ListOffsetsResult latestOffsetsResult =
                adminClient.listOffsets(requestLatestOffsets);

        double totalLag = 0;

        for (TopicPartition tp : consumerOffsets.keySet()) {

            long committedOffset = consumerOffsets.get(tp).offset();

            long latestOffset =
                    latestOffsetsResult.partitionResult(tp).get().offset();

            totalLag += (latestOffset - committedOffset);
        }

        lagValue = totalLag;
    }

    public double getLag() {
        return lagValue;
    }
}