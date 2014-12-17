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
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kaboka
 */
public class Aggregator {

    private static Map<String, Aggregate> activeAggregates;

    private static Channel channelIn;
    private static Channel channelOut;
    private static final String IN_QUEUE = "aggregator_gr1";
    private static final String OUT_QUEUE = "webservice_gr1";
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
        startTimer();
        handleMessage();
    }

    private static void handleMessage() throws IOException, InterruptedException {
        while (true) {
           
            Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            String correlationID = delivery.getProperties().getCorrelationId();
            System.out.println("Message recived: " + message);
            Aggregate aggregate = (Aggregate) activeAggregates.get(correlationID);
            if (aggregate == null) {
                aggregate = new LoanAggregate(new BankLoan());
                aggregate.addMessage(message);
                activeAggregates.put(correlationID, aggregate);
            } else {
                aggregate.addMessage(message);
            }
            publishResult(aggregate, correlationID);
        }
    }

    private static void publishResult(Aggregate aggregate, String correlationID) throws IOException {
        if (aggregate.isComplete()) {
            String resultMessage = aggregate.getResultMessage();
            System.out.println("Message Published: " + resultMessage);
            channelOut.basicPublish("", OUT_QUEUE, null, resultMessage.getBytes());
            System.out.println("published: ");
            activeAggregates.remove(correlationID);
        }
    }

    private static void startTimer() {
        Thread timer = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        System.out.println("Sleeping");
                        Thread.sleep(5000);
                        System.out.println("Running");
                        for (Entry<String, Aggregate> entry : activeAggregates.entrySet()) {
                            publishResult(entry.getValue(), entry.getKey());
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Aggregator.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Aggregator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        timer.start();
    }
}
