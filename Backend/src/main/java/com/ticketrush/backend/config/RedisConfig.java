package com.ticketrush.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Cấu hình Redis, RedisTemplate và ObjectMapper dùng chung.
 *
 * Annotation {@link Configuration} đăng ký các bean hạ tầng Redis cho Spring.
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    /**
     * Tạo kết nối Lettuce đến Redis standalone.
     *
     * @return factory kết nối Redis.
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(redisHost, redisPort)
        );
    }

    /**
     * Tạo {@link RedisTemplate} xử lý key, value và hash bằng chuỗi.
     *
     * Annotation {@link Primary} ưu tiên template này khi có nhiều bean cùng loại.
     *
     * @param connectionFactory factory kết nối Redis.
     * @return template thao tác Redis với key và value kiểu chuỗi.
     */
    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);

        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Tạo container lắng nghe Redis Pub/Sub.
     *
     * Bean này phục vụ các luồng realtime cần nhận message từ Redis, không bắt
     * buộc nếu chỉ dùng Redis làm cache.
     *
     * @param connectionFactory factory kết nối Redis.
     * @return container lắng nghe message Redis.
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    /**
     * Tạo {@link ObjectMapper} dùng chung và hỗ trợ kiểu ngày giờ Java 8.
     *
     * @return mapper JSON dùng chung trong ứng dụng.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
