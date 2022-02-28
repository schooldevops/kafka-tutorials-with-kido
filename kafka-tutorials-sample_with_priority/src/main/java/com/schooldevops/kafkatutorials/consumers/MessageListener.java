package com.schooldevops.kafkatutorials.consumers;

import com.schooldevops.kafkatutorials.configs.KafkaTopicConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
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
}
