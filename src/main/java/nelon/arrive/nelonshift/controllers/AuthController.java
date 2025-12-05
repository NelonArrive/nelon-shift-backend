package nelon.arrive.nelonshift.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.dto.auth.*;
import nelon.arrive.nelonshift.entities.RefreshToken;
import nelon.arrive.nelonshift.entities.Role;
import nelon.arrive.nelonshift.entities.User;
import nelon.arrive.nelonshift.exceptions.AlreadyExistsException;
import nelon.arrive.nelonshift.exceptions.TokenRefreshException;
import nelon.arrive.nelonshift.repositories.RoleRepository;
import nelon.arrive.nelonshift.repositories.UserRepository;
import nelon.arrive.nelonshift.security.CustomUserDetails;
import nelon.arrive.nelonshift.security.jwt.JwtTokenProvider;
import nelon.arrive.nelonshift.services.RefreshTokenService;
import nelon.arrive.nelonshift.services.TokenBlacklistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

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
	private final RefreshTokenService refreshTokenService;
	private final TokenBlacklistService tokenBlacklistService;
	
	@PostMapping("/login")
	public ResponseEntity<?> login(
		@Valid @RequestBody LoginRequest loginRequest,
		HttpServletRequest request
	) {
		log.info("Login attempt for email: {}", loginRequest.getEmail());
		
		Authentication authentication;
		
		try {
			authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
					loginRequest.getEmail(),
					loginRequest.getPassword()
				)
			);
		} catch (BadCredentialsException e) {
			log.warn("Bad credentials for user: {}", loginRequest.getEmail());
			throw new nelon.arrive.nelonshift.exceptions.BadCredentialsException(
				"Invalid email or password"
			);
		} catch (Exception e) {
			log.error("Login error for {}: {}", loginRequest.getEmail(), e.getMessage());
			throw new nelon.arrive.nelonshift.exceptions.BadCredentialsException(
				"Invalid email or password"
			);
		}
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		// Генерируем access token
		String jwt = jwtTokenProvider.generateToken(authentication);
		
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		
		// Создаем refresh token
		String ipAddress = getClientIp(request);
		String userAgent = request.getHeader("User-Agent");
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(
			userDetails.getId(),
			userDetails.getEmail(),
			ipAddress,
			userAgent
		);
		
		Set<String> roles = userDetails.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.toSet());
		
		log.info("Login successful for user: {}", userDetails.getEmail());
		
		return ResponseEntity.ok(JwtResponse.builder()
			.accessToken(jwt)
			.refreshToken(refreshToken.getToken())
			.tokenType("Bearer")
			.id(userDetails.getId())
			.email(userDetails.getEmail())
			.name(userDetails.getName())
			.roles(roles)
			.build());
	}
	
	@PostMapping("/signup")
	public ResponseEntity<?> signup(
		@Valid @RequestBody SignupRequest signupRequest
	) {
		log.info("Signup attempt for email: {}", signupRequest.getEmail());
		
		if (userRepository.existsByEmail(signupRequest.getEmail())) {
			throw new AlreadyExistsException("Email already exists");
		}
		
		User user = User.builder()
			.email(signupRequest.getEmail())
			.password(passwordEncoder.encode(signupRequest.getPassword()))
			.name(signupRequest.getName())
			.build();
		
		Set<Role> roles = new HashSet<>();
		
		if (signupRequest.getRoles() == null || signupRequest.getRoles().isEmpty()) {
			Role userRole = roleRepository.findByName("ROLE_USER")
				.orElseThrow(() -> new ResourceAccessException("Role ROLE_USER not found"));
			roles.add(userRole);
		} else {
			signupRequest.getRoles().forEach(roleName -> {
				Role role = roleRepository.findByName(roleName)
					.orElseThrow(() -> new ResourceAccessException("Role " + roleName + " not found"));
				roles.add(role);
			});
		}
		
		user.setRoles(roles);
		userRepository.save(user);
		
		return ResponseEntity.ok(new MessageResponse("User registered successfully"));
	}
	
	/**
	 * Обновление access token используя refresh token
	 */
	@PostMapping("/refresh")
	public ResponseEntity<?> refreshToken(
		@Valid @RequestBody RefreshTokenRequest request,
		HttpServletRequest httpRequest
	) {
		String requestRefreshToken = request.getRefreshToken();
		
		log.info("Refresh token request");
		
		return refreshTokenService.findByToken(requestRefreshToken)
			.map(refreshTokenService::verifyExpiration)
			.map(refreshToken -> {
				// Генерируем новый access token
				String newAccessToken = jwtTokenProvider.generateTokenFromEmail(
					refreshToken.getUserEmail()
				);
				
				// Ротация refresh token (опционально, но рекомендуется для безопасности)
				String ipAddress = getClientIp(httpRequest);
				String userAgent = httpRequest.getHeader("User-Agent");
				RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(
					requestRefreshToken,
					ipAddress,
					userAgent
				);
				
				log.info("Token refreshed successfully for user: {}", refreshToken.getUserEmail());
				
				return ResponseEntity.ok(JwtResponse.builder()
					.accessToken(newAccessToken)
					.refreshToken(newRefreshToken.getToken())
					.tokenType("Bearer")
					.build());
			})
			.orElseThrow(() -> new TokenRefreshException(
				"Refresh token is not in database!"
			));
	}
	
	/**
	 * Logout - отзываем текущие токены
	 */
	@PostMapping("/logout")
	public ResponseEntity<?> logout(
		@Valid @RequestBody RefreshTokenRequest request,
		HttpServletRequest httpRequest
	) {
		String refreshToken = request.getRefreshToken();
		
		// Удаляем refresh token
		refreshTokenService.deleteByToken(refreshToken);
		
		// Добавляем access token в blacklist
		String jwt = parseJwt(httpRequest);
		if (jwt != null) {
			tokenBlacklistService.blacklistToken(jwt);
		}
		
		log.info("User logged out successfully");
		
		return ResponseEntity.ok(new MessageResponse("Logout successful"));
	}
	
	/**
	 * Logout со всех устройств
	 */
	@PostMapping("/logout-all")
	public ResponseEntity<?> logoutAll(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		HttpServletRequest httpRequest
	) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new MessageResponse("Unauthorized"));
		}
		
		// Удаляем все refresh tokens пользователя
		refreshTokenService.deleteAllUserTokens(userDetails.getId());
		
		// Добавляем текущий access token в blacklist
		String jwt = parseJwt(httpRequest);
		if (jwt != null) {
			tokenBlacklistService.blacklistToken(jwt);
		}
		
		log.info("User logged out from all devices: {}", userDetails.getEmail());
		
		return ResponseEntity.ok(new MessageResponse("Logged out from all devices"));
	}
	
	@GetMapping("/me")
	public ResponseEntity<?> getCurrentUser(
		@AuthenticationPrincipal CustomUserDetails user
	) {
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("Unauthorized");
		}
		
		Set<String> roles = user.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.toSet());
		
		return ResponseEntity.ok(JwtResponse.builder()
			.id(user.getId())
			.email(user.getEmail())
			.name(user.getName())
			.roles(roles)
			.build());
	}
	
	// Вспомогательные методы
	
	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");
		
		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7);
		}
		
		return null;
	}
	
	private String getClientIp(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null) {
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0];
	}
}