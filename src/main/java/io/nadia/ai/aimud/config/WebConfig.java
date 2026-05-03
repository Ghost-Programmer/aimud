package io.nadia.ai.aimud.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nadia.ai.aimud.types.EffectType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Application configuration wrapper for WebConfig.
 */
@Configuration
public class WebConfig implements WebFluxConfigurer {

    /**
     * Configures the component for add formatters.
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToEffectTypeConverter());
    }

    @Bean
    public ObjectMapper jackson2ObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public static class StringToEffectTypeConverter implements Converter<String, EffectType> {
    /**
     * Configures the component for convert.
     * @return constructed EffectType dependency
     */
        @Override
        public EffectType convert(String source) {
            return EffectType.fromString(source);
        }
    }
}
