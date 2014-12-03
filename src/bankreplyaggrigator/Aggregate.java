/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bankreplyaggrigator;

import com.rabbitmq.client.QueueingConsumer.Delivery;

/**
 *
 * @author Kaboka
 */
public interface Aggregate {

    public void addMessage(String message);
    public boolean isComplete();
    public String getResultMessage();

}
