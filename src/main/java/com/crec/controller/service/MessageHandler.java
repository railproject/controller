package com.crec.controller.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class MessageHandler implements MessageListener{

	public void onMessage(Message arg0) {
		System.out.println("-----------------------testListener--------------------------");
		System.out.println(arg0);
		System.out.println("-----------------------testListener--------------------------");
		
	}

}
