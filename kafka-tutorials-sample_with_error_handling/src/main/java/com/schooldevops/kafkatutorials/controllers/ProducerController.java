package com.schooldevops.kafkatutorials.controllers;

import com.schooldevops.kafkatutorials.configs.KafkaTopicConfig;
import com.schooldevops.kafkatutorials.entities.TestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api")
public class ProducerController {

    @Value("${kafka.topic-with-key}")
    public String TOPIC_WITH_KEY;

    @Value("${kafka.topic-with-priority}")
    public String TOPIC_WITH_PRIORITY;

    @Value("${kafka.topic-default-error}")
    public String TOPIC_COMMON_ERROR;

    @Value("${kafka.topic-error-handle}")
    public String TOPIC_ERROR_HANDLER;

    @Value("${kafka.topic-retry-handle}")
    public String TOPIC_RETRY_HANDLER;

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

    @PostMapping("produce-with-key/{key}")
    public ResponseEntity<?> produceMessageWithKey(@PathVariable("key") String key, @RequestBody TestEntity testEntity) {
        testEntity.setTime(LocalDateTime.now());

        ListenableFuture<SendResult<String, Object>> future = kafkaProducerTemplate.send(TOPIC_WITH_KEY, key, testEntity);

        future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Unable to send message: {}", ex.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, Object> result) {
                log.info("Sent message with key: {}, offset: {}, partition: {}", key, result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
            }
        });

        return ResponseEntity.ok(testEntity);
    }

    @PostMapping("produce-with-priority/{key}")
    public ResponseEntity<?> produceMessageWithPriority(@PathVariable("key") String key, @RequestBody TestEntity testEntity) {
        testEntity.setTime(LocalDateTime.now());

        ListenableFuture<SendResult<String, Object>> future = kafkaProducerTemplate.send(TOPIC_WITH_PRIORITY, key, testEntity);

        future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Unable to send message: {}", ex.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, Object> result) {
                log.info("Sent message with priority: {}, offset: {}, partition: {}", key, result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
            }
        });

        return ResponseEntity.ok(testEntity);
    }

    @PostMapping("produce_error/{category}")
    public ResponseEntity<?> produceMessage(@PathVariable("category") String category, @RequestBody TestEntity testEntity) {
        testEntity.setTime(LocalDateTime.now());

        String topic = "";
        if ("default-error".equals(category)) {
            topic = TOPIC_COMMON_ERROR;
        } else if ("error-handler".equals(category)) {
            topic = TOPIC_ERROR_HANDLER;
        } else if ("retry-handler".equals(category)) {
            topic = TOPIC_RETRY_HANDLER;
        } else {
            topic = KafkaTopicConfig.DEFAULT_TOPIC;
        }

        ListenableFuture<SendResult<String, Object>> future = kafkaProducerTemplate.send(topic, testEntity);

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
