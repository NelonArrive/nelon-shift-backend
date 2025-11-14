package nelon.arrive.nelonshift.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
	
	@Value("${jwt.secret}")
	private String jwtSecret;
	
	@Value("${jwt.expiration}")
	private Long jwtExpirationMs;
	
	public String generateToken(Authentication authentication) {
		CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
		SecretKey key = getSigningKey();
		
		Instant now = Instant.now();
		Instant expiry = now.plusMillis(jwtExpirationMs);
		
		return Jwts.builder()
			.subject(principal.getUsername())
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiry))
			.signWith(getSigningKey())
			.compact();
	}
	
	public String getUserEmailFromToken(String token) {
		SecretKey key = getSigningKey();
		
		Claims claims = Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
		
		return claims.getSubject();
	}
	
	public boolean validateToken(String token) {
		try {
			SecretKey key = getSigningKey();
			Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			log.error("JWT validation failed: {}", e.getMessage());
		}
		return false;
	}
	
	private SecretKey getSigningKey() {
		byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
		return new SecretKeySpec(keyBytes, "HmacSHA256");
	}
	
}
