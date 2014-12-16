/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bankreplyaggrigator;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import dk.cphbusiness.connection.ConnectionCreator;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Kaboka
 */
public class Aggregator {

    private static Map<String, Aggregate> activeAggregates;

    private static Channel channelIn;
    private static Channel channelOut;
    private static final String IN_QUEUE = "aggregator";
    private static final String OUT_QUEUE = "webservice";
    private static QueueingConsumer consumer;

    public static void main(String[] args) throws IOException, InterruptedException {
        activeAggregates = new HashMap();
        ConnectionCreator creator = ConnectionCreator.getInstance();

        channelIn = creator.createChannel();
        channelOut = creator.createChannel();
        channelIn.queueDeclare(IN_QUEUE, false, false, false, null);
        channelOut.queueDeclare(OUT_QUEUE, false, false, false, null);
        consumer = new QueueingConsumer(channelIn);
        channelIn.basicConsume(IN_QUEUE, true, consumer);
        handleMessage();
    }

    public static void handleMessage() throws IOException, InterruptedException {
        while (true) {
            Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            String correlationID = delivery.getProperties().getCorrelationId();
            System.out.println(correlationID);
            Aggregate aggregate = (Aggregate) activeAggregates.get(correlationID);
            if (aggregate == null) {
                aggregate = new LoanAggregate(new BankLoan());
                aggregate.addMessage(message);
                activeAggregates.put(correlationID, aggregate);
            } else {
                aggregate.addMessage(message);
            }
            if (!aggregate.isComplete()) {
                aggregate.addMessage(message);
            }
            if (aggregate.isComplete()) {
                channelOut.basicPublish("", OUT_QUEUE, null, aggregate.getResultMessage().getBytes());
                System.out.println("published: ");
                activeAggregates.remove(correlationID);
            }
        }
    }
}
