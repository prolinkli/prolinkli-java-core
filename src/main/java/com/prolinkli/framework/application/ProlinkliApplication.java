package com.prolinkli.framework.application;

import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ProlinkliApplication
 */
@SpringBootApplication(scanBasePackages = "com.prolinkli")
public class ProlinkliApplication {

	private final static Logger LOGGER =
			org.slf4j.LoggerFactory.getLogger(ProlinkliApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ProlinkliApplication.class, args);
		LOGGER.info("Prolinkli Application started successfully.");
	}

}
