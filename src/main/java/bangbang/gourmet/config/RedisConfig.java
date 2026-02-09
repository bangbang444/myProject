package bangbang.gourmet.config;

import bangbang.gourmet.common.security.jwt.RedisProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 연결 및 직렬화 설정
 * Lettuce 클라이언트를 사용하여 Redis에 연결하고, Repository 패턴을 활성화합니다.
 */
@Configuration
@RequiredArgsConstructor
@EnableRedisRepositories(basePackages = "bangbang.gourmet.common.security.jwt.repository")
public class RedisConfig {

    private final RedisProperties redisProperties;

    /**
     * Redis 연결 팩토리 생성
     * Lettuce 클라이언트를 사용 (Spring Boot 기본, 비동기 지원)
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.host());
        config.setPort(redisProperties.port());

        if (redisProperties.password() != null && !redisProperties.password().isEmpty()) {
            config.setPassword(redisProperties.password());
        }

        return new LettuceConnectionFactory(config);
    }

    /**
     * RedisTemplate 설정
     * Key와 Value의 직렬화 방식을 정의합니다.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // Key는 String으로 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        // Value는 JSON으로 직렬화
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}