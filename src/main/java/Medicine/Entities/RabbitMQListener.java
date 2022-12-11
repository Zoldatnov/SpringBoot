package Medicine.Entities;

import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@EnableRabbit
@Component
public class RabbitMQListener {
    Logger logger = Logger.getLogger(RabbitMQListener.class);

    @RabbitListener(queues = "medQueue")
    public void processQueue1(String message) {
        logger.info(message);
    }
}