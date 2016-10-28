package com.tonyzampogna.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;


/**
 * Main application file.
 */
@SpringBootApplication
@ComponentScan("com.tonyzampogna")
public class App extends SpringBootServletInitializer {
	private static final Logger log = LoggerFactory.getLogger(App.class);


	/**
	 * This method runs on applications start.
	 */
	public static void main(String[] args) {
		log.info("Starting application");
		SpringApplication.run(App.class, args);
	}
}

