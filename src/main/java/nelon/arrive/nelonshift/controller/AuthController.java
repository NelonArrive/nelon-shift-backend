package nelon.arrive.nelonshift.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.exception.AlreadyExistsException;
import nelon.arrive.nelonshift.repository.UserRepository;
import nelon.arrive.nelonshift.request.LoginRequest;
import nelon.arrive.nelonshift.request.SignupRequest;
import nelon.arrive.nelonshift.response.ApiResponse;
import nelon.arrive.nelonshift.response.JwtResponse;
import nelon.arrive.nelonshift.security.jwt.JwtUtils;
import nelon.arrive.nelonshift.security.user.CustomUserDetails;
import nelon.arrive.nelonshift.services.interfaces.IAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/auth")
public class AuthController {
	
	private final IAuthService authService;
	
	@PostMapping("/login")
	public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
		JwtResponse jwtResponse = authService.login(loginRequest);
		return ResponseEntity.ok(new ApiResponse("User login successfully", jwtResponse));
	}
	
	@PostMapping("/register")
	public ResponseEntity<ApiResponse> register(@Valid @RequestBody SignupRequest signupRequest) {
		authService.register(signupRequest);
		return ResponseEntity.ok(new ApiResponse("User registered successfully", null));
	}
}
