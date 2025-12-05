package nelon.arrive.nelonshift.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
	
	private final RedisTemplate<String, Object> redisTemplate;
	
	@Value("${jwt.access-token-expiration}")
	private Long accessTokenDurationMs;
	
	private static final String BLACKLIST_PREFIX = "blacklist:";
	
	/**
	 * Добавить JWT токен в blacklist
	 */
	public void blacklistToken(String token) {
		String key = BLACKLIST_PREFIX + token;
		redisTemplate.opsForValue().set(
			key,
			"blacklisted",
			accessTokenDurationMs,
			TimeUnit.MILLISECONDS
		);
		log.info("Token added to blacklist");
	}
	
	/**
	 * Проверить, находится ли токен в blacklist
	 */
	public boolean isTokenBlacklisted(String token) {
		String key = BLACKLIST_PREFIX + token;
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}
}