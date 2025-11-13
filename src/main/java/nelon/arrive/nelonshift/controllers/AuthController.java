package nelon.arrive.nelonshift.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.auth.JwtResponse;
import nelon.arrive.nelonshift.dto.auth.LoginRequest;
import nelon.arrive.nelonshift.dto.auth.MessageResponse;
import nelon.arrive.nelonshift.dto.auth.SignupRequest;
import nelon.arrive.nelonshift.entities.Role;
import nelon.arrive.nelonshift.entities.User;
import nelon.arrive.nelonshift.repositories.RoleRepository;
import nelon.arrive.nelonshift.repositories.UserRepository;
import nelon.arrive.nelonshift.security.CustomUserDetails;
import nelon.arrive.nelonshift.services.AuthService;
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
public class AuthController {
	
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthService authService;
	
	@PostMapping("/login")
	public ResponseEntity<?> login(
		@Valid @RequestBody LoginRequest loginRequest
	) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(
				loginRequest.getEmail(),
				loginRequest.getPassword()
			)
		);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		String jwt = authService.generateJwtToken(authentication);
		
		CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
		Set<String> roles = customUserDetails.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.toSet());
		
		return ResponseEntity.ok(new JwtResponse(
			jwt,
			customUserDetails.getId(),
			customUserDetails.getEmail(),
			customUserDetails.getName(),
			roles
		));
	}
	
	@PostMapping("/signup")
	public ResponseEntity<?> signup(
		@Valid @RequestBody SignupRequest signupRequest
	) {
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
		CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
		
		Set<String> roles = customUserDetails.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.toSet());
		
		return ResponseEntity.ok(new JwtResponse(
			null,
			customUserDetails.getId(),
			customUserDetails.getEmail(),
			customUserDetails.getName(),
			roles
		));
	}
}
