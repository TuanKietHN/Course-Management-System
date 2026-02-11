package vn.com.nws.cms.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import vn.com.nws.cms.common.config.RabbitMqConfig;
import vn.com.nws.cms.common.dto.EmailMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmail(EmailMessage message) {
        log.info("Sending email message: {}", message);
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EMAIL_EXCHANGE,
                RabbitMqConfig.EMAIL_ROUTING_KEY,
                message
        );
    }
}
