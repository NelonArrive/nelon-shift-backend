package nelon.arrive.nelonshift.exceptions;

import org.springframework.http.HttpStatus;

public class BadCredentialsException extends ApiException {
	public BadCredentialsException(String message) {
		super(HttpStatus.UNAUTHORIZED, message);
	}
}
