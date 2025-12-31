package nelon.arrive.nelonshift.services;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.exception.AlreadyExistsException;
import nelon.arrive.nelonshift.repository.UserRepository;
import nelon.arrive.nelonshift.request.LoginRequest;
import nelon.arrive.nelonshift.request.SignupRequest;
import nelon.arrive.nelonshift.response.JwtResponse;
import nelon.arrive.nelonshift.security.jwt.JwtUtils;
import nelon.arrive.nelonshift.security.user.CustomUserDetails;
import nelon.arrive.nelonshift.services.interfaces.IAuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
	
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtils jwtUtils;
	
	@Override
	public JwtResponse login(LoginRequest request) {
		Authentication auth = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(
				request.getEmail(),
				request.getPassword()
			)
		);
		
		SecurityContextHolder.getContext().setAuthentication(auth);
		String jwt = jwtUtils.generateTokenForUser(auth);
		CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
		
		return new JwtResponse(userDetails.getId(), jwt);
	}
	
	@Override
	public User register(SignupRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new AlreadyExistsException("Email already in use");
		}
		
		User user = User.builder()
			.email(request.getEmail())
			.password(passwordEncoder.encode(request.getPassword()))
			.name(request.getName())
			.build();
		
		return userRepository.save(user);
	}
}

