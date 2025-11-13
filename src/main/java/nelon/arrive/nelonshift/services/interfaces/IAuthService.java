package nelon.arrive.nelonshift.services.interfaces;

import org.springframework.security.core.userdetails.UserDetails;

public interface IAuthService {
	UserDetails authenticate(String email, String password);
	String generateJwtToken(UserDetails userDetails);
	UserDetails validateJwtToken(String token);
}
