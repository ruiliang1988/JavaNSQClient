package com.trendrr.nsq;
/**
 * 
 */


import java.io.IOException;
import java.io.UnsupportedEncodingException;
//import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.trendrr.oss.StringHelper;




/**
 * @author Dustin Norlander
 * @created Jan 14, 2013
 * 
 */
public class ExampleMain {

	protected static Log log = LogFactory.getLog(ExampleMain.class);
	static AtomicInteger processed = new AtomicInteger(0);
	static Date start;
	/**
	 * @param args
	 */
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
		/*
		 * PRODUCER.  produce 50k messages
		 */
		//producer
		NSQProducer producer = new NSQProducer(50).addAddress("localhost", 4150);		
		producer.start();
		start = new Date();
		String msg = StringHelper.randomString(10000);

		for (int i=0; i < 50000; i++) {
			if (i % 1000 == 0) {
				System.out.println("producer: " + i);
			}
			producer.produce("speedtest", (msg + i).getBytes("utf8"));
		}
		
//		My System does this in about 10 seconds, so 5k messages per second on a single connection
		System.out.println("Produced 50k messages in " + (new Date().getTime()-start.getTime()) + " millis");
		
		
		
        NSQLookup lookup = new NSQLookup();
        lookup.addAddr("localhost", 4161);
		
		start = new Date();
		/**
		 * Consumer.  consume 50k messages and immediately exit
		 */
		NSQConsumer consumer = new NSQConsumer(lookup, "speedtest", "dustin", new MessageCallback() {
			
			@Override
			public void message(NSQMessage message) {
				try {
					
					//now mark the message as finished.
					message.finished();
					
					//or you could requeue it, which indicates a failure and puts it back on the queue.
//					message.requeue();

					int p = processed.incrementAndGet();
					if (p % 1000 == 0) {
						System.out.println("consumer: " + p);
					}
					if (p % 50000 == 0) {
						System.out.println("completed 50k in " + (new Date().getTime()-start.getTime()));
						start = new Date();
						System.exit(1);
					}
					
				} catch (Exception e) {
					log.error("Caught", e);
				}
			}
			
			@Override
			public void error(Exception x) {
				log.warn("Caught", x);
			}
		});
		
		consumer.start();
		
		
		
	}
	

	
}
