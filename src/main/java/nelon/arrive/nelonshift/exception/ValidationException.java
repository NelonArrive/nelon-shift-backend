package nelon.arrive.nelonshift.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

@Getter
public class ValidationException extends ApiException {
	
	private final Map<String, String> errors;
	
	public ValidationException(String message, Map<String, String> errors) {
		super(HttpStatus.BAD_REQUEST, message);
		this.errors = errors;
	}
	
	public ValidationException(String message) {
		super(HttpStatus.BAD_REQUEST, message);
		this.errors = Collections.emptyMap();
	}
}