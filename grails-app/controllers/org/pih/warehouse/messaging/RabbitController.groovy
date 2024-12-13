package org.pih.warehouse.messaging

class RabbitController {

    RabbitProducerService rabbitProducerService
    RabbitConsumerService rabbitConsumerService

    def send() {
        String message = params.message ?: "Hello from Grails!"
        rabbitProducerService.sendMessage(message)
        render "Message sent: '${message}'"
    }

    def consume() {
        Thread.start {
            rabbitConsumerService.startConsumer()
        }
        render "Consumer started. Check console for messages."
    }
}
