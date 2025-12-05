package nelon.arrive.nelonshift.exceptions;

import org.springframework.http.HttpStatus;

public class TokenRefreshException extends ApiException {
	
	public TokenRefreshException(String message) {
		super(HttpStatus.FORBIDDEN, message);
	}
	
	public TokenRefreshException(String message, Throwable cause) {
		super(HttpStatus.FORBIDDEN, message, cause);
	}
}