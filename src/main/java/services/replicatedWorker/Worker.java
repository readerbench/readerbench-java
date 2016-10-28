/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services.replicatedWorker;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;


/**
 * 
 * @author Mihai Dascalu
 */
public abstract class Worker implements MessageListener, ExceptionListener {
	static Logger logger = Logger.getLogger("");

	private boolean running;

	protected Session sessionTask;
	protected Session sessionMsg;
	protected Destination destinationTask;
	protected Destination destinationMsg;
	protected MessageProducer status;
	protected Connection connection;

	private boolean transacted;
	private boolean durable;
	protected String workerID;

	private class Keepalive extends Thread {
		public void run() {
			while (true) {
				try {
					Thread.sleep(Master.WORKER_KEEP_ALIVE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					send(new StatusMsg(workerID, StatusMsg.KEEPALIVE_MESSAGE,
							null, null));
				} catch (Exception e) {
					logger.severe(workerID
							+ " exception while sending keepalive message; now exiting.");
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}

	}

	public void run() {
		try {
			running = true;
			logger.info("Connecting to URL: " + Master.URL);
			logger.info("Using a " + (durable ? "durable" : "non-durable")
					+ " subscription");

			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					Master.USER, Master.PASSWORD, Master.URL);
			connection = connectionFactory.createConnection();
			connection.setExceptionListener(this);
			connection.start();
			workerID = connection.getClientID();

			// create session for sending messages
			sessionMsg = connection.createSession(transacted,
					Session.AUTO_ACKNOWLEDGE);
			destinationMsg = sessionMsg.createQueue(Master.MESSAGES_QUEUE);

			// Create the producer.
			status = sessionMsg.createProducer(destinationMsg);
			if (Master.PERSISTENT) {
				status.setDeliveryMode(DeliveryMode.PERSISTENT);
			} else {
				status.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			}

			// say hello
			send(new StatusMsg(workerID, StatusMsg.HELLO_MESSAGE, null, null));

			// start keep-alive process
			Thread keep = new Keepalive();
			keep.start();

			// create session for receiving tasks
			sessionTask = connection.createSession(transacted,
					Session.AUTO_ACKNOWLEDGE);
			destinationTask = sessionTask.createQueue(Master.TASK_QUEUE + "_"
					+ workerID);

			MessageConsumer consumer = null;
			consumer = sessionTask.createConsumer(destinationTask);

			consumer.setMessageListener(this);
		} catch (Exception e) {
			logger.severe("Caught: " + e);
			e.printStackTrace();
		}
	}

	public void onMessage(Message message) {
		try {
			if (message instanceof ObjectMessage)
				try {
					performTask(((ObjectMessage) message).getObject());
				} catch (Exception e) {
					System.err.println(workerID + " error");
					e.printStackTrace();
				}

			if (transacted) {
				sessionTask.commit();
			}

		} catch (JMSException e) {
			logger.severe("Caught: " + e);
			e.printStackTrace();
		}
	}

	public void send(StatusMsg msg) throws Exception {

		ObjectMessage message = sessionMsg.createObjectMessage(msg);

		status.send(message);
		if (transacted) {
			sessionMsg.commit();
		}
	}

	public abstract void performTask(Serializable task) throws Exception;

	public synchronized void onException(JMSException ex) {
		logger.severe("JMS Exception occured. Shutting down client.");
		running = false;
	}

	synchronized boolean isRunning() {
		return running;
	}
}
