package com.schooldevops.kafkatutorials.consumers;

import com.schooldevops.kafkatutorials.configs.KafkaTopicConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageListener {

    @KafkaListener(topics = KafkaTopicConfig.DEFAULT_TOPIC, containerFactory = "defaultKafkaListenerContainerFactory")
    public void listenDefaultTopic(Object record) {
        log.info("Receive Message from {}, values {}", KafkaTopicConfig.DEFAULT_TOPIC, record);

    }
}
