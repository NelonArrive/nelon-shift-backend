package nelon.arrive.nelonshift.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.entities.RefreshToken;
import nelon.arrive.nelonshift.exceptions.TokenRefreshException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
	
	private final RedisTemplate<String, Object> redisTemplate;
	
	@Value("${jwt.refresh-token-expiration}")
	private Long refreshTokenDurationMs;
	
	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
	private static final String USER_REFRESH_TOKENS_PREFIX = "user_refresh_tokens:";
	
	/**
	 * Создать новый refresh token
	 */
	public RefreshToken createRefreshToken(UUID userId, String userEmail, String ipAddress, String userAgent) {
		// Генерируем уникальный токен
		String token = UUID.randomUUID().toString();
		
		RefreshToken refreshToken = RefreshToken.builder()
			.token(token)
			.userId(userId)
			.userEmail(userEmail)
			.expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
			.createdAt(Instant.now())
			.ipAddress(ipAddress)
			.userAgent(userAgent)
			.build();
		
		// Сохраняем в Redis
		String key = REFRESH_TOKEN_PREFIX + token;
		try {
			redisTemplate.opsForValue().set(
				key,
				refreshToken,
				refreshTokenDurationMs,
				TimeUnit.MILLISECONDS
			);
			log.info("✅ Saved refresh token to Redis: {} for user: {}", token, userEmail);
			
			// Проверка что токен сохранился
			Object saved = redisTemplate.opsForValue().get(key);
			log.info("✅ Verification - token exists in Redis: {}", saved != null);
		} catch (Exception e) {
			log.error("❌ Failed to save refresh token to Redis: ", e);
			throw e;
		}
		
		// Добавляем токен в список токенов пользователя (для отзыва всех токенов)
		String userTokensKey = USER_REFRESH_TOKENS_PREFIX + userId;
		redisTemplate.opsForSet().add(userTokensKey, token);
		redisTemplate.expire(userTokensKey, refreshTokenDurationMs, TimeUnit.MILLISECONDS);
		
		log.info("Created refresh token for user: {}", userEmail);
		return refreshToken;
	}
	
	/**
	 * Найти refresh token по токену
	 */
	public Optional<RefreshToken> findByToken(String token) {
		String key = REFRESH_TOKEN_PREFIX + token;
		log.info("🔍 Looking for refresh token: {}", key);
		
		try {
			Object value = redisTemplate.opsForValue().get(key);
			log.info("🔍 Found value in Redis: {}", value != null ? value.getClass().getName() : "null");
			
			if (value == null) {
				log.warn("❌ Refresh token not found in Redis: {}", token);
				return Optional.empty();
			}
			
			if (value instanceof RefreshToken) {
				log.info("✅ Successfully retrieved RefreshToken");
				return Optional.of((RefreshToken) value);
			}
			
			log.error("❌ Value is not RefreshToken, it's: {}", value.getClass().getName());
			return Optional.empty();
		} catch (Exception e) {
			log.error("❌ Error retrieving refresh token from Redis: ", e);
			return Optional.empty();
		}
	}
	
	/**
	 * Проверить валидность refresh token
	 */
	public RefreshToken verifyExpiration(RefreshToken token) {
		if (token.isExpired()) {
			deleteByToken(token.getToken());
			throw new TokenRefreshException(
				"Refresh token was expired. Please make a new signin request"
			);
		}
		return token;
	}
	
	/**
	 * Удалить refresh token
	 */
	public void deleteByToken(String token) {
		String key = REFRESH_TOKEN_PREFIX + token;
		RefreshToken refreshToken = (RefreshToken) redisTemplate.opsForValue().get(key);
		
		if (refreshToken != null) {
			// Удаляем из списка токенов пользователя
			String userTokensKey = USER_REFRESH_TOKENS_PREFIX + refreshToken.getUserId();
			redisTemplate.opsForSet().remove(userTokensKey, token);
			
			// Удаляем сам токен
			redisTemplate.delete(key);
			log.info("Deleted refresh token for user: {}", refreshToken.getUserEmail());
		}
	}
	
	/**
	 * Удалить все refresh токены пользователя (logout from all devices)
	 */
	public void deleteAllUserTokens(UUID userId) {
		String userTokensKey = USER_REFRESH_TOKENS_PREFIX + userId;
		var tokens = redisTemplate.opsForSet().members(userTokensKey);
		
		if (tokens != null) {
			tokens.forEach(token -> {
				String key = REFRESH_TOKEN_PREFIX + token;
				redisTemplate.delete(key);
			});
			redisTemplate.delete(userTokensKey);
			log.info("Deleted all refresh tokens for user: {}", userId);
		}
	}
	
	/**
	 * Ротация refresh token (создаем новый, удаляем старый)
	 */
	public RefreshToken rotateRefreshToken(String oldToken, String ipAddress, String userAgent) {
		RefreshToken oldRefreshToken = findByToken(oldToken)
			.orElseThrow(() -> new TokenRefreshException("Refresh token not found"));
		
		verifyExpiration(oldRefreshToken);
		
		// Удаляем старый токен
		deleteByToken(oldToken);
		
		// Создаем новый
		return createRefreshToken(
			oldRefreshToken.getUserId(),
			oldRefreshToken.getUserEmail(),
			ipAddress,
			userAgent
		);
	}
}