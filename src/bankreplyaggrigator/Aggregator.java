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

    public Aggregator(Channel input, Channel output) {
        activeAggregates = new HashMap();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("nicklas");
        factory.setPassword("cph");
        factory.setHost("datdb.cphbusiness.dk");

        Connection connection = factory.newConnection();
        channelIn = connection.createChannel();
        channelOut = connection.createChannel();
        channelIn.queueDeclare(IN_QUEUE, false, false, false, null);
        channelOut.queueDeclare(OUT_QUEUE, false, false, false, null);
 
        handleMessage();

        
    }

    public static void handleMessage() throws IOException, InterruptedException {

        while (true) {
            QueueingConsumer consumer = new QueueingConsumer(channelIn);
            channelIn.basicConsume(IN_QUEUE, true, consumer);
            Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            String correlationID = delivery.getProperties().getCorrelationId();
            Aggregate aggregate = (Aggregate) activeAggregates.get(correlationID);
            if (aggregate == null) {
                aggregate = new LoanAggregate(new BankLoan());
                activeAggregates.put(correlationID, aggregate);

            }
            //--- ignore message if aggregate is already closed
            if (!aggregate.isComplete()) {
                aggregate.addMessage(message);
                if (aggregate.isComplete()) {
                    channelOut.basicPublish("", OUT_QUEUE, null, aggregate.getResultMessage().getBytes());
                    activeAggregates.remove(correlationID);
                }
            }
        }
    }
}
