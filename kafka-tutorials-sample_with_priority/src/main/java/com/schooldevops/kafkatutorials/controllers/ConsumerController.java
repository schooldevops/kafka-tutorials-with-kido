package com.schooldevops.kafkatutorials.controllers;

import com.schooldevops.kafkatutorials.configs.KafkaTopicConfig;
import com.schooldevops.kafkatutorials.consumers.ManualConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("api/consumer")
public class ConsumerController {

    private final ManualConsumerService manualConsumerService;

    public ConsumerController(ManualConsumerService manualConsumerService) {
        this.manualConsumerService = manualConsumerService;
    }

    @GetMapping("consume")
    public ResponseEntity<?> getMessage(
            @RequestParam(value = "partition", required = false, defaultValue = "0") Integer partition,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset
    ) {
        return ResponseEntity.ok(manualConsumerService.receiveMessages(KafkaTopicConfig.DEFAULT_TOPIC, partition, offset));

    }
}
