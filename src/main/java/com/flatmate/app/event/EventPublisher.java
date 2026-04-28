package com.flatmate.app.event;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final SqsTemplate sqsTemplate;

    public void publishEvent(String queueName, Object event) {
        try {
            sqsTemplate.send(queueName, event);
            log.debug("Published event to {}: {}", queueName, event);
        } catch (Exception e) {
            log.error("Failed to publish event to {}: {}", queueName, e.getMessage());
            // Fallback for local dev if SQS is not available
            System.out.println("EVENT [" + queueName + "]: " + event);
        }
    }
}
