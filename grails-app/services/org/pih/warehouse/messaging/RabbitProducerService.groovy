package org.pih.warehouse.messaging

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Connection
import com.rabbitmq.client.Channel

class RabbitProducerService {

    static final String QUEUE_NAME = "grails-queue"
    static final String HOST = "localhost"

    def sendMessage(String message) {
        Connection connection = null
        Channel channel = null

        try {
            ConnectionFactory factory = new ConnectionFactory()
            factory.host = HOST

            connection = factory.newConnection()
            channel = connection.createChannel()

            channel.queueDeclare(QUEUE_NAME, false, false, false, null)

            channel.basicPublish("", QUEUE_NAME, null, message.bytes)
            println "Sent message: '${message}' to queue: ${QUEUE_NAME}"

        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (channel != null) {
                try {
                    channel.close()
                } catch (Exception ex) {
                    ex.printStackTrace()
                }
            }
            if (connection != null) {
                try {
                    connection.close()
                } catch (Exception ex) {
                    ex.printStackTrace()
                }
            }
        }
    }
}
