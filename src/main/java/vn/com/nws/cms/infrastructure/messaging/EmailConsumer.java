package vn.com.nws.cms.infrastructure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import vn.com.nws.cms.common.config.RabbitMqConfig;
import vn.com.nws.cms.common.dto.EmailMessage;

@Service
@Slf4j
public class EmailConsumer {

    @RabbitListener(queues = RabbitMqConfig.EMAIL_QUEUE)
    public void receiveEmail(@Payload EmailMessage message) {
        log.info("Received email message: {}", message);
        processEmail(message);
    }

    private void processEmail(EmailMessage message) {
        log.info(
                "Processing email | to={} | subject={} | type={}",
                message.getTo(),
                message.getSubject(),
                message.getType()
        );

        try {
            Thread.sleep(1000); // simulate sending
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Email sent successfully to {}", message.getTo());
    }
}
