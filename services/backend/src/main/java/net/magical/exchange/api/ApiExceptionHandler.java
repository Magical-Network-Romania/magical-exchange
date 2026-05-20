package net.magical.exchange.api;

import net.magical.exchange.service.exception.BadRequestException;
import net.magical.exchange.service.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ProblemDetail> handleBadRequest(BadRequestException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());

		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ProblemDetail> handleNotFound(NotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}
}
