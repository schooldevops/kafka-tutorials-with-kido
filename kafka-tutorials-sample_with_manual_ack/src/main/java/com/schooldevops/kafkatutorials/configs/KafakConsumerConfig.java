package com.schooldevops.kafkatutorials.configs;

import com.schooldevops.kafkatutorials.entities.RetryTestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

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
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, KafkaProperties.IsolationLevel.READ_COMMITTED.toString().toLowerCase());

        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();
        // Deserialize에 대해서 신뢰하는 패키지를 지정한다. "*"를 지정하면 모두 신뢰하게 된다.
        jsonDeserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    private ConsumerFactory<String, Object> consumerFactoryWithNoAutoCommit(String groupId) {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, KafkaProperties.IsolationLevel.READ_COMMITTED.toString().toLowerCase());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();
        // Deserialize에 대해서 신뢰하는 패키지를 지정한다. "*"를 지정하면 모두 신뢰하게 된다.
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
        // SeekToCurrentErrorHandler 의 경우, 현재 읽은 오프셋에서 에러가 발생하면 FixedBackOff 등으로 설정한 backoff 만큼 기다리다가 다시 메시지를 읽는다.
        // FixedBackOff(주기(밀리세컨), 최대재시도횟수) 로 백오프를 지정했다.
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
        // SeekToCurrentErrorHandler 의 경우, 현재 읽은 오프셋에서 에러가 발생하면 FixedBackOff 등으로 설정한 backoff 만큼 기다리다가 다시 메시지를 읽는다.
        // FixedBackOff(주기(밀리세컨), 최대재시도횟수) 로 백오프를 지정했다.
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

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> manualAckKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactoryWithNoAutoCommit("manualAckGroup"));
        factory.setConcurrency(1);
        factory.setAutoStartup(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
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
     * 수동 컨슈머를 작성한다.
     * 기존과 다른것은 Consumer 객체를 반환하는 것이다.
     * @return 컨슈머를 반환합니다.
     */
    @Bean
    public Consumer<String, Object> manualConsumer() {
        return consumerFactory("manualConsumerGroup").createConsumer();
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> transactionalKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("consumerGroupTransactional"));
        factory.setConcurrency(1);
        factory.setAutoStartup(true);
        return factory;
    }
}
