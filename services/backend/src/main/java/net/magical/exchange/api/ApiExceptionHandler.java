package net.magical.exchange.api;

import jakarta.servlet.http.HttpServletRequest;
import net.magical.exchange.service.exception.BadRequestException;
import net.magical.exchange.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ProblemDetail> handleBadRequest(BadRequestException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());

		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ProblemDetail> handleMissingRequestParameter(MissingServletRequestParameterException exception) {
		String detail = "Missing required query parameter: " + exception.getParameterName();
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);

		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
		String detail = "Invalid value for query parameter: " + exception.getName();
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);

		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ProblemDetail> handleNotFound(NotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleInternalServerError(Exception exception, HttpServletRequest request) {
		LOGGER.error("Unhandled API failure for {}", request.getRequestURI(), exception);

		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"Internal server error. Check backend logs for the stack trace.");

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
	}
}
