package com.schooldevops.kafkatutorials.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import javax.annotation.PostConstruct;

@Configuration
public class KafkaTopicConfig {

    public final static String DEFAULT_TOPIC = "DEF_TOPIC";

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

    @Autowired
    private KafkaAdmin kafkaAdmin;

    private NewTopic defaultTopic() {
        return TopicBuilder.name(DEFAULT_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    private NewTopic topicWithKey() {
        return TopicBuilder.name(TOPIC_WITH_KEY)
                .partitions(1)
                .replicas(1)
                .build();
    }

    private NewTopic topicWithPriority() {
        return TopicBuilder.name(TOPIC_WITH_PRIORITY)
                .partitions(1)
                .replicas(1)
                .build();
    }

    private NewTopic topicCommonError() {
        return TopicBuilder.name(TOPIC_COMMON_ERROR)
                .partitions(1)
                .replicas(1)
                .build();
    }

    private NewTopic topicErrorHandle() {
        return TopicBuilder.name(TOPIC_ERROR_HANDLER)
                .partitions(1)
                .replicas(1)
                .build();
    }

    private NewTopic topicRetryHandle() {
        return TopicBuilder.name(TOPIC_RETRY_HANDLER)
                .partitions(1)
                .replicas(1)
                .build();
    }

    private NewTopic topicTransactional() {
        return TopicBuilder.name(TOPIC_TRANSACTION)
                .partitions(1)  //
                .replicas(1)
                .build();
    }

    @PostConstruct
    public void init() {
        kafkaAdmin.createOrModifyTopics(defaultTopic());
        kafkaAdmin.createOrModifyTopics(topicWithKey());
        kafkaAdmin.createOrModifyTopics(topicWithPriority());
        kafkaAdmin.createOrModifyTopics(topicCommonError());
        kafkaAdmin.createOrModifyTopics(topicErrorHandle());
        kafkaAdmin.createOrModifyTopics(topicRetryHandle());
        kafkaAdmin.createOrModifyTopics(topicTransactional());
    }
}
