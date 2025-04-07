package com.example.client.config;

import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonCustomizer {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer addProtobufModule() {
        return builder -> builder.modulesToInstall(ProtobufModule.class);
    }
}
