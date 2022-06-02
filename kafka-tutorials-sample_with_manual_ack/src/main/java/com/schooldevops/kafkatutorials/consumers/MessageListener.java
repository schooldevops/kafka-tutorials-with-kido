package com.schooldevops.kafkatutorials.consumers;

import com.schooldevops.kafkatutorials.configs.KafkaTopicConfig;
import com.schooldevops.kafkatutorials.entities.RetryTestException;
import com.schooldevops.kafkatutorials.entities.TestEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageListener {

    @KafkaListener(topics = KafkaTopicConfig.DEFAULT_TOPIC, containerFactory = "defaultKafkaListenerContainerFactory")
    public void listenDefaultTopic(Object record) {
        log.info("Receive Message from {}, values {}", KafkaTopicConfig.DEFAULT_TOPIC, record);
    }

    @KafkaListener(topics = "${kafka.topic-with-key}", containerFactory = "defaultKafkaListenerContainerFactory")
    public void listenTopicWithKey(Object record) {
        log.info("Receive Message from {}, values {} with key", record);
    }

    @KafkaListener(topics = "${kafka.topic-with-priority}", containerFactory = "highPriorityKafkaListenerContainerFactory")
    public void listenPriorityTopic(Object record) {
        log.info("Received high priority message: {}", record);
    }

    @KafkaListener(topics = "${kafka.topic-with-priority}", containerFactory = "normalPriorityKafkaListenerContainerFactory")
    public void listenNonPriorityTopic(Object record) {
        log.info("Received normal priority message: {}", record);
    }

    @KafkaListener(topics = "${kafka.topic-default-error}", containerFactory = "errorCommonHandlingKafkaListenerContainerFactory")
    public void listenForDefaultErrorHandle(Object record) {
        log.info("Receive Message for Default Error Handler, It will occur error: {}", record);
        throw new RuntimeException("Consumer Error and Exception Occurs.");
    }

    @KafkaListener(topics = "${kafka.topic-error-handle}", containerFactory = "errorHandlingKafkaListenerContainerFactory")
    public void listenForErrorHandle(Object record) {
        log.info("Receive Message for Error Handler, It will occur error: {}", record);
        throw new RuntimeException("Consumer Error and Exception Occurs.");
    }

    @KafkaListener(topics = "${kafka.topic-retry-handle}", containerFactory = "recoveryHandlingKafkaListenerContainerFactory")
    public void listenForRetryHandle(Object record) {
        log.info("Receive Message for Retry Handler, It will occur error: {}", record);
        throw new RetryTestException("Consumer Error and Exception Occurs.");
    }

    @KafkaListener(topics = "${kafka.transactional-topic}", containerFactory = "transactionalKafkaListenerContainerFactory")
    public void listenForTransactionalTopic(Object record) {
        log.info("Received Transactional message: {}", record);
    }

    @KafkaListener(topics = "${kafka.manual-ack-topic}", containerFactory = "manualAckKafkaListenerContainerFactory")
    public void listenManualAck(Object record, Acknowledgment ack) {
        log.info("Received Transactional message: {}", record);

        ConsumerRecord<String, TestEntity> recordData = (ConsumerRecord<String, TestEntity>) record;

        if (recordData.offset() % 5 == 0) {
            ack.acknowledge();
        }
    }
}
