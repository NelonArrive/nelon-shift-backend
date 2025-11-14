package nelon.arrive.nelonshift.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.dto.auth.JwtResponse;
import nelon.arrive.nelonshift.dto.auth.LoginRequest;
import nelon.arrive.nelonshift.dto.auth.MessageResponse;
import nelon.arrive.nelonshift.dto.auth.SignupRequest;
import nelon.arrive.nelonshift.entities.Role;
import nelon.arrive.nelonshift.entities.User;
import nelon.arrive.nelonshift.repositories.RoleRepository;
import nelon.arrive.nelonshift.repositories.UserRepository;
import nelon.arrive.nelonshift.security.CustomUserDetails;
import nelon.arrive.nelonshift.security.jwt.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/${api.prefix}/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
	
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	
	@PostMapping("/login")
	public ResponseEntity<?> login(
		@Valid @RequestBody LoginRequest loginRequest
	) {
		log.info("Login attempt for email: {}", loginRequest.getEmail());
		
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(
				loginRequest.getEmail(),
				loginRequest.getPassword()
			)
		);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		String jwt = jwtTokenProvider.generateToken(authentication);
		
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		Set<String> roles = userDetails.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.toSet());
		
		log.info("Login successful for user: {}", userDetails.getEmail());
		
		return ResponseEntity.ok(new JwtResponse(
			jwt,
			userDetails.getId(),
			userDetails.getEmail(),
			userDetails.getName(),
			roles
		));
	}
	
	@PostMapping("/signup")
	public ResponseEntity<?> signup(
		@Valid @RequestBody SignupRequest signupRequest
	) {
		log.info("Signup attempt for email: {}", signupRequest.getEmail());
		
		if (userRepository.existsByEmail(signupRequest.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}
		
		User user = User.builder()
			.email(signupRequest.getEmail())
			.password(passwordEncoder.encode(signupRequest.getPassword()))
			.name(signupRequest.getName())
			.build();
		
		Set<Role> roles = new HashSet<>();
		
		if (signupRequest.getRoles() == null || signupRequest.getRoles().isEmpty()) {
			Role userRole = roleRepository.findByName("ROLE_USER")
				.orElseThrow(() -> new IllegalArgumentException("Role ROLE_USER not found"));
			roles.add(userRole);
		} else {
			signupRequest.getRoles().forEach(roleName -> {
				Role role = roleRepository.findByName(roleName)
					.orElseThrow(() -> new IllegalArgumentException("Role " + roleName + " not found"));
				roles.add(role);
			});
		}
		
		user.setRoles(roles);
		userRepository.save(user);
		
		return ResponseEntity.ok(new MessageResponse("User registered successfully"));
	}
	
	@GetMapping("/me")
	public ResponseEntity<?> getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		
		Set<String> roles = userDetails.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.toSet());
		
		return ResponseEntity.ok(new JwtResponse(
			null,
			userDetails.getId(),
			userDetails.getEmail(),
			userDetails.getName(),
			roles
		));
	}
}
