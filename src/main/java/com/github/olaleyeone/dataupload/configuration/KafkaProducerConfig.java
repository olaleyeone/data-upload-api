package com.github.olaleyeone.dataupload.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    public <E> ProducerFactory<String, E> objectProducerFactory(ObjectMapper objectMapper) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        return new DefaultKafkaProducerFactory<>(configProps, () -> new StringSerializer(), () -> new JsonSerializer(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ObjectMapper objectMapper) {
        return new KafkaTemplate<>(objectProducerFactory(objectMapper));
    }
}
