package net.magical.exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MagicalExchangeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MagicalExchangeApplication.class, args);
	}
}
