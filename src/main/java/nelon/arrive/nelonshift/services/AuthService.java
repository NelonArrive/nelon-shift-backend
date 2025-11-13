package nelon.arrive.nelonshift.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.services.interfaces.IAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService implements IAuthService {
	
	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	
	@Value("${jwt.secret}")
	private String jwtSecret;
	
	@Value("${jwt.expiration}")
	private int jwtExpiryMs;
	
	@Override
	public UserDetails authenticate(String email, String password) {
		authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(email, password)
		);
		return userDetailsService.loadUserByUsername(email);
	}
	
	@Override
	public String generateJwtToken(UserDetails userDetails) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + jwtExpiryMs);
		
		return Jwts.builder()
			.subject(userDetails.getUsername())
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(getSigninKey())
			.compact();
	}
	
	@Override
	public UserDetails validateJwtToken(String authToken) {
		String userEmail = getUserEmailFromJwtToken(authToken);
		return userDetailsService.loadUserByUsername(userEmail);
	}
	
	public String getUserEmailFromJwtToken(String token) {
		return Jwts.parser()
			.verifyWith(getSigninKey())
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getSubject();
	}
	
	private SecretKey getSigninKey() {
		byte[] keyBytes = jwtSecret.getBytes();
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
