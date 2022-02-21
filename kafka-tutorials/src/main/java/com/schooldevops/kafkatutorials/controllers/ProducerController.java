package com.schooldevops.kafkatutorials.controllers;

import com.schooldevops.kafkatutorials.configs.KafkaTopicConfig;
import com.schooldevops.kafkatutorials.entities.TestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api")
public class ProducerController {

    private final KafkaTemplate<String, Object> kafkaProducerTemplate;

    public ProducerController(KafkaTemplate<String, Object> kafkaProducerTemplate) {
        this.kafkaProducerTemplate = kafkaProducerTemplate;
    }

    @PostMapping("produce")
    public ResponseEntity<?> produceMessage(@RequestBody TestEntity testEntity) {
        testEntity.setTime(LocalDateTime.now());

        ListenableFuture<SendResult<String, Object>> future = kafkaProducerTemplate.send(KafkaTopicConfig.DEFAULT_TOPIC, testEntity);

        future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Fail to send message to broker: {}", ex.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, Object> result) {
                log.info("Send message with offset: {}, partition: {}", result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
            }
        });

        return ResponseEntity.ok(testEntity);

    }
}
