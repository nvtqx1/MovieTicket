package com.ticketrush.backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;

import org.springframework.kafka.annotation.EnableKafka;

import java.util.HashMap;
import java.util.Map;

/**
 * Cấu hình Kafka cho producer, consumer và topic xử lý yêu cầu đặt vé.
 *
 * Annotation {@link EnableKafka} kích hoạt cơ chế lắng nghe Kafka qua
 * {@code @KafkaListener}; {@link Configuration} đăng ký các bean cấu hình.
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // ==========================================
    // TOPIC
    // ==========================================

    /**
     * Tạo topic {@code ticket_requests} nếu chưa tồn tại.
     *
     * Topic dùng một partition để giữ thứ tự xử lý FIFO cho các yêu cầu đặt vé.
     *
     * @return topic Kafka cho hàng đợi yêu cầu đặt vé.
     */
    @Bean
    public NewTopic ticketRequestsTopic() {
        return TopicBuilder.name("ticket_requests")
                .partitions(1)
                .replicas(1)
                .build();
    }

    // ==========================================
    // PRODUCER
    // ==========================================

    /**
     * Tạo factory cấu hình producer Kafka dùng key và value kiểu chuỗi.
     *
     * @return factory dùng để tạo Kafka producer.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Tạo {@link KafkaTemplate} để gửi thông điệp chuỗi lên Kafka.
     *
     * @return template gửi thông điệp Kafka.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ==========================================
    // CONSUMER
    // ==========================================

    /**
     * Tạo factory cấu hình consumer Kafka dùng key và value kiểu chuỗi.
     *
     * Consumer đọc từ offset sớm nhất khi chưa có offset và giới hạn mỗi lần poll
     * tối đa 50 record để kiểm soát tốc độ xử lý.
     *
     * @return factory dùng để tạo Kafka consumer.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Giới hạn mỗi lần poll chỉ lấy 50 record để kiểm soát tốc độ xử lý.
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Tạo factory cho các listener Kafka.
     *
     * Concurrency bằng 1 để chỉ chạy một consumer thread, giúp giữ thứ tự xử lý
     * của topic một partition.
     *
     * @return factory container cho {@code @KafkaListener}.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // Chỉ 1 consumer thread để đảm bảo thứ tự và giới hạn tốc độ xử lý.
        factory.setConcurrency(1);
        return factory;
    }
}
