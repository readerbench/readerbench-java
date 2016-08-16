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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

/**
 * 
 * @author Mihai Dascalu
 */
public abstract class Master implements MessageListener, ExceptionListener {
	static Logger logger = Logger.getLogger(Master.class);

	public static final String USER = ActiveMQConnection.DEFAULT_USER;
	public static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;
	public static final String URL = "tcp://localhost:61616";
	public static final String TASK_QUEUE = "TASKS";
	public static final String MESSAGES_QUEUE = "MESSAGES";
	public static final long WORKER_KEEP_ALIVE = 1000;
	public static final boolean PERSISTENT = false;

	private static final double FAST_SF = 2.0 / (2.0 + 1);
	private static final int MAX_WAIT = 10;
	private static final double ALPHA = 0.5;
	private static final double SLOW_SF = 2.0 / (10.0 + 1);
	private static final int N = 5;// window size
	private static final double MAX_SLACK = 0.8;
	private static final long START_TIME = System.currentTimeMillis();

	protected boolean running;
	protected Destination destinationMsg;

	protected boolean transacted;
	protected Session sessionMsg;
	protected Map<String, Session> sessionTask;
	protected Map<String, Destination> destinationTask;
	protected Map<String, MessageProducer> master;
	protected Connection connection;
	protected int noTasks;

	protected Map<String, String> assignedTasks;
	protected Map<String, Long> startTime;
	protected Map<String, Long> endTime;
	protected Map<String, List<String>> completedTasks;
	protected int noCompletedTasks;

	protected Map<String, Double> alpha;
	protected Map<String, List<Long>> A;
	protected Map<String, List<Long>> P;

	public abstract void performBeforeComplete();

	public abstract void analyseResult(Object result);

	private class Monitoring implements Runnable {
		public void run() {
			List<String> diedWorkers;
			while (true) {
				try {
					Thread.sleep(WORKER_KEEP_ALIVE / 10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				synchronized (completedTasks) {
					if (noCompletedTasks == noTasks) {
						// finish for master
						logger.info("\nMaster successfully finished performing all tasks!");
						logger.info("Final statistics:");
						for (String addr : completedTasks.keySet()) {
							double computingTime = 0;
							for (String name : completedTasks.get(addr))
								computingTime += endTime.get(name)
										- startTime.get(name);
							logger.info("\t" + addr + "\t"
									+ completedTasks.get(addr).size()
									+ " tasks\t" + computingTime / 1000 + "s");
						}
						try {
							connection.close();
						} catch (JMSException e) {
							e.printStackTrace();
						}
						// perform other tasks before completion
						performBeforeComplete();
						System.exit(-1);
					}
				}
				diedWorkers = new LinkedList<String>();

				for (String addr : A.keySet()) {
					List<Long> crtA = A.get(addr);
					synchronized (crtA) {
						if (crtA.size() != N) {
							if (System.currentTimeMillis() - START_TIME > crtA
									.get(crtA.size() - 1)
									+ MAX_WAIT
									* WORKER_KEEP_ALIVE) {
								// worker died => mark for deletion
								diedWorkers.add(addr);
							}
						} else {
							// perform t
							double tnow = System.currentTimeMillis()
									- START_TIME
									- P.get(addr).get(P.get(addr).size() - 1);
							double tpred = P.get(addr).get(
									P.get(addr).size() - 1)
									- P.get(addr).get(P.get(addr).size() - 2);
							double t = tnow / tpred;
							double sl = (Math.exp(ALPHA * (t - 1)) - 1)
									/ (Math.exp(ALPHA * (t - 1)) + 1);
							if (sl > MAX_SLACK)
								// worker died => mark for deletion
								diedWorkers.add(addr);
						}
					}
				}
				// perform cleanup
				for (String addr : diedWorkers) {
					logger.error(addr + " died!");
					// reassign task
					logger.info("Master reassigning " + assignedTasks.get(addr));
					reassignTask(assignedTasks.get(addr));
					cleanup(addr);
				}
			}
		}
	}

	public void cleanup(String addr) {
		sessionTask.remove(addr);
		assignedTasks.remove(addr);
		destinationTask.remove(addr);
		master.remove(addr);
		alpha.remove(addr);
		A.remove(addr);
		P.remove(addr);
	}

	public void run() {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				USER, PASSWORD, URL);
		assignedTasks = new TreeMap<String, String>();
		completedTasks = new TreeMap<String, List<String>>();
		sessionTask = new TreeMap<String, Session>();
		destinationTask = new TreeMap<String, Destination>();
		master = new TreeMap<String, MessageProducer>();
		startTime = new TreeMap<String, Long>();
		endTime = new TreeMap<String, Long>();
		alpha = new TreeMap<String, Double>();
		A = new TreeMap<String, List<Long>>();
		P = new TreeMap<String, List<Long>>();

		connection = null;
		try {
			logger.info("Master online: " + URL);
			logger.info("Master connecting to URL: " + URL);
			logger.info("Using "
					+ (PERSISTENT ? "persistent" : "non-persistent")
					+ " messages");

			// Create the connection
			connection = connectionFactory.createConnection();
			connection.setClientID("Master");
			connection.setExceptionListener(this);
			connection.start();

			// now waiting for workers to say hello
			sessionMsg = connection.createSession(transacted,
					Session.AUTO_ACKNOWLEDGE);
			destinationMsg = sessionMsg.createQueue(MESSAGES_QUEUE);

			MessageConsumer consumer = null;
			consumer = sessionMsg.createConsumer(destinationMsg);
			consumer.setMessageListener(this);

			// monitor evolution
			Thread monitor = new Thread(new Monitoring());
			monitor.start();
		} catch (Exception e) {
			logger.info("Caught: " + e);
			e.printStackTrace();
		}
	}

	public void onMessage(Message message) {
		try {
			if (message instanceof ObjectMessage) {
				StatusMsg msg = (StatusMsg) (((ObjectMessage) message)
						.getObject());

				String addr = msg.getSourceID();

				switch (msg.getType()) {
				case StatusMsg.KEEPALIVE_MESSAGE:
					if (A.containsKey(addr)) {
						// update activity
						synchronized (A.get(addr)) {
							A.get(addr).add(
									System.currentTimeMillis() - START_TIME);
							if (A.get(addr).size() > N)
								A.get(addr).remove(0);
						}
						// if currently nothing add the last inserted item
						long At_1 = A.get(addr).get(A.get(addr).size() - 1);
						synchronized (P.get(addr)) {
							long Pt_1 = P.get(addr).get(P.get(addr).size() - 1);
							P.get(addr).add(
									(long) (alpha.get(addr) * At_1 + Pt_1
											* (1 - alpha.get(addr))));
							if (P.get(addr).size() > N)
								P.get(addr).remove(0);
						}
						if (A.get(addr).size() == N) {
							List<Long> crtA = A.get(addr);
							long direction = crtA.get(0)
									- crtA.get(crtA.size() - 1);
							long volatility = 0;
							for (int i = 0; i < crtA.size() - 1; i++)
								volatility += Math.abs(crtA.get(i + 1)
										- crtA.get(i));
							double ER = ((double) Math.abs(direction))
									/ Math.abs(volatility);
							double ASF = ER * (FAST_SF - SLOW_SF) + SLOW_SF;

							double newAlpha = Math.pow(ASF, 2);
							alpha.put(addr, newAlpha);
						}
					}
					break;

				case StatusMsg.STARTING_MESSAGE:
					logger.info("Master received: " + msg);
					assignedTasks.put(addr, msg.getArguments()[0]);
					startTime.put(msg.getArguments()[0],
							System.currentTimeMillis());
					break;

				case StatusMsg.FINISHED_MESSAGE:
					logger.info("Master received: " + msg);
					// remove current execution
					if (assignedTasks.containsKey(addr)) {
						assignedTasks.remove(addr);
						// add job to completed ones
						if (!completedTasks.containsKey(addr))
							completedTasks.put(addr, new LinkedList<String>());
						completedTasks.get(addr).add(msg.getArguments()[0]);
						noCompletedTasks++;
						endTime.put(msg.getArguments()[0],
								System.currentTimeMillis());
						analyseResult(msg.getResult());
						// checkpoint
						checkpoint();
						// send a new task or finish worker
						try {
							sendTask(addr);
						} catch (Exception e) {
							logger.error("Error assigning task!");
							e.printStackTrace();
						}
					}
					break;

				case StatusMsg.HELLO_MESSAGE:
					logger.info("Master received: " + msg);
					// Create the session
					alpha.put(addr, 2.0 / (N + 1));
					A.put(addr, new LinkedList<Long>());
					A.get(addr).add(System.currentTimeMillis() - START_TIME);
					P.put(addr, new LinkedList<Long>());
					P.get(addr).add(A.get(addr).get(A.get(addr).size() - 1));

					sessionTask.put(addr, connection.createSession(transacted,
							Session.AUTO_ACKNOWLEDGE));
					destinationTask.put(addr, sessionTask.get(addr)
							.createQueue(TASK_QUEUE + "_" + addr));

					// Create the producer
					master.put(
							addr,
							sessionTask.get(addr).createProducer(
									destinationTask.get(addr)));
					if (PERSISTENT) {
						master.get(addr).setDeliveryMode(
								DeliveryMode.PERSISTENT);
					} else {
						master.get(addr).setDeliveryMode(
								DeliveryMode.NON_PERSISTENT);
					}
					try {
						sendTask(addr);
					} catch (Exception e) {
						logger.error("Error assigning task!");
						e.printStackTrace();
					}
					break;
				}
			}

			if (transacted) {
				sessionMsg.commit();
			}
		} catch (JMSException e) {
			logger.info("Caught: " + e);
			e.printStackTrace();
		}
	}

	public synchronized void onException(JMSException ex) {
		logger.error("JMS Exception occured. Shutting down master.");
		running = false;
	}

	synchronized boolean isRunning() {
		return running;
	}

	public abstract void sendTask(String addr) throws Exception;

	public abstract void reassignTask(String task);

	public abstract void checkpoint();
}
