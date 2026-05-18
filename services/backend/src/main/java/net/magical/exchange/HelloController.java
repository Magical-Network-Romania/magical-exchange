package net.magical.exchange;

import java.time.OffsetDateTime;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class HelloController {

	@GetMapping("/api/hello")
	public HelloResponse hello() {
		return new HelloResponse("Hello from Magical Exchange API", OffsetDateTime.now());
	}

	@GetMapping("/api/ping")
	public PingResponse ping() {
		return new PingResponse("pong");
	}

	@GetMapping("/api/soroja")
	public PingResponse soroja() {
		return new PingResponse("Soroja is the best!");
	}

	public record HelloResponse(String message, OffsetDateTime timestamp) {
	}

	public record PingResponse(String message) {
	}
}
