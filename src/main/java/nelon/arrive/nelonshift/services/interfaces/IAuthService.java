package nelon.arrive.nelonshift.services.interfaces;

import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.request.LoginRequest;
import nelon.arrive.nelonshift.request.SignupRequest;
import nelon.arrive.nelonshift.response.JwtResponse;

public interface IAuthService {
	JwtResponse login(LoginRequest request);
	
	User register(SignupRequest request);
}
