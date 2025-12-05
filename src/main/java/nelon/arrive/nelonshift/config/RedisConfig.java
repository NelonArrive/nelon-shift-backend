package nelon.arrive.nelonshift.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
	
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		
		// Настройка ObjectMapper для правильной сериализации
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		
		// Включаем type information для правильной десериализации
		PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
			.allowIfSubType(Object.class)
			.build();
		objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
		
		// Используем String для ключей
		StringRedisSerializer stringSerializer = new StringRedisSerializer();
		template.setKeySerializer(stringSerializer);
		template.setHashKeySerializer(stringSerializer);
		
		// Используем JSON для значений
		GenericJackson2JsonRedisSerializer jsonSerializer =
			new GenericJackson2JsonRedisSerializer(objectMapper);
		template.setValueSerializer(jsonSerializer);
		template.setHashValueSerializer(jsonSerializer);
		
		template.afterPropertiesSet();
		return template;
	}
}