package org.pih.warehouse.messaging

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Connection
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery

class RabbitConsumerService {

    static final String QUEUE_NAME = "grails-queue"
    static final String HOST = "localhost"

    def startConsumer() {
        Connection connection = null
        Channel channel = null

        try {
            ConnectionFactory factory = new ConnectionFactory()
            factory.host = HOST

            connection = factory.newConnection()
            channel = connection.createChannel()

            channel.queueDeclare(QUEUE_NAME, false, false, false, null)
            println "Waiting for messages from queue: ${QUEUE_NAME}"

            DeliverCallback deliverCallback = new DeliverCallback() {
                @Override
                void handle(String consumerTag, Delivery delivery) {
                    String message = new String(delivery.getBody(), "UTF-8")
                    println "Received message: '${message}'"
                }
            }

            channel.basicConsume(QUEUE_NAME, true, deliverCallback, new CancelCallback() {
                @Override
                void handle(String consumerTag) {
                    println "Consumer canceled: ${consumerTag}"
                }
            })
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
