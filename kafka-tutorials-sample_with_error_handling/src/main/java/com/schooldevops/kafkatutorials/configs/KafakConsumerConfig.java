package com.schooldevops.kafkatutorials.configs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.schooldevops.kafkatutorials.entities.RetryTestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@EnableKafka
@Configuration
public class KafakConsumerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServer;

    private ConsumerFactory<String, Object> consumerFactory(String groupId) {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();
        // Deserialize??? ????????? ???????????? ???????????? ????????????. "*"??? ???????????? ?????? ???????????? ??????.
        jsonDeserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> defaultKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("defaultGroup"));
        factory.setConcurrency(1);
        factory.setAutoStartup(true);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> highPriorityKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("consumerGroupHighPriority"));
        factory.setRecordFilterStrategy(consumerRecord -> !"highPriority".equals(consumerRecord.key()));
        factory.setConcurrency(1);
        factory.setAutoStartup(true);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> normalPriorityKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("consumerGroupNormalPriority"));
        factory.setRecordFilterStrategy(consumerRecord -> "highPriority".equals(consumerRecord.key()));
        factory.setConcurrency(1);
        factory.setAutoStartup(true);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> errorHandlingKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("errorHandlingConsumerGroup"));
        // SeekToCurrentErrorHandler ??? ??????, ?????? ?????? ??????????????? ????????? ???????????? FixedBackOff ????????? ????????? backoff ?????? ??????????????? ?????? ???????????? ?????????.
        // FixedBackOff(??????(????????????), ?????????????????????) ??? ???????????? ????????????.
        factory.setErrorHandler(new ErrorHandler() {
            @Override
            public void handle(Exception thrownException, ConsumerRecord<?, ?> data) {
                log.error("Error is {} : data {}", thrownException.getMessage(), data);
            }
        });
        factory.setConcurrency(1);
        factory.setAutoStartup(true);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> errorCommonHandlingKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("errorHandlingConsumerGroup"));
        // SeekToCurrentErrorHandler ??? ??????, ?????? ?????? ??????????????? ????????? ???????????? FixedBackOff ????????? ????????? backoff ?????? ??????????????? ?????? ???????????? ?????????.
        // FixedBackOff(??????(????????????), ?????????????????????) ??? ???????????? ????????????.
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(100, 2)));
        factory.setConcurrency(1);
        factory.setAutoStartup(true);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> recoveryHandlingKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("retryHandlingConsumerGroup"));
        factory.setRetryTemplate(retryTemplate());
        factory.setRecoveryCallback(new RecoveryCallback<Object>() {
            @Override
            public Object recover(RetryContext retryContext) throws Exception {
                ConsumerRecord consumerRecord = (ConsumerRecord) retryContext.getAttribute("record");
                log.info("Recovery is called for message {} ", consumerRecord.value());
                return null;
            }
        });
        factory.setConcurrency(1);
        factory.setAutoStartup(true);
        return factory;
    }

    private RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(getSimpleRetryPolicy());
        return retryTemplate;
    }

    private SimpleRetryPolicy getSimpleRetryPolicy() {
        Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<>();
        exceptionMap.put(RetryTestException.class, true);
        return new SimpleRetryPolicy(5,exceptionMap,true);
    }

    /**
     * ?????? ???????????? ????????????.
     * ????????? ???????????? Consumer ????????? ???????????? ?????????.
     * @return ???????????? ???????????????.
     */
    @Bean
    public Consumer<String, Object> manualConsumer() {
        return consumerFactory("manualConsumerGroup").createConsumer();
    }

}
