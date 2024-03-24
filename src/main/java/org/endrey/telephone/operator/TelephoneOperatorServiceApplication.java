package org.endrey.telephone.operator;

import org.endrey.telephone.operator.controller.MainLoop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TelephoneOperatorServiceApplication implements CommandLineRunner {

	@Autowired
	private MainLoop mainLoop;

	public static void main(String[] args) {
		SpringApplication.run(TelephoneOperatorServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mainLoop.run();
	}

}
