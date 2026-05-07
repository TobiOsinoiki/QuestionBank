package com.tobi.qbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuestionbankApplication {

	public static void main(String[] args) {
	      System.setProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");
	        System.setProperty("com.sun.net.ssl.checkRevocation", "false");
	        System.setProperty("trustSystemCacerts", "true");
		
		SpringApplication.run(QuestionbankApplication.class, args);
	}

}
