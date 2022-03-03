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
import java.util.ArrayList;
import java.util.List;

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

    @Value("${kafka.transactional-topic}")
    public String TOPIC_TRANSACTION;

    private final KafkaTemplate<String, Object> kafkaProducerTemplate;

    private final KafkaTemplate<String, Object> kafkaTransactionalProducerTemplate;

    public ProducerController(KafkaTemplate<String, Object> kafkaProducerTemplate, KafkaTemplate<String, Object> kafkaTransactionalProducerTemplate) {
        this.kafkaProducerTemplate = kafkaProducerTemplate;
        this.kafkaTransactionalProducerTemplate = kafkaTransactionalProducerTemplate;
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

    @PostMapping("produce/transactional")
    public ResponseEntity<?> sendTransctional(@RequestBody List<TestEntity> testEntities) {

        List<TestEntity> entityList = new ArrayList<>();
        if (testEntities == null || testEntities.isEmpty()) {
            return ResponseEntity.status(404).body("Not Valid Request Parameters");
        }

        LocalDateTime localTime = LocalDateTime.now();
        kafkaTransactionalProducerTemplate.executeInTransaction(kafkaOperations -> {
            for (TestEntity entity: testEntities) {
                entity.setTime(localTime);

                // title 엔터티가 TITLE: 로 시작하는경우 정상진행
                if (entity.getTitle() != null && entity.getTitle().startsWith("TITLE:")) {
                    kafkaOperations.send(TOPIC_TRANSACTION, entity.getTitle(), entity);
                    entityList.add(entity);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new RuntimeException("Error Exception by not valid title: " + entity.getTitle());
                }
            }

            return null;
        });

        return ResponseEntity.ok(entityList);
    }

}
