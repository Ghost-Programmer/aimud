package io.nadia.ai.aimud.config;

import io.nadia.ai.aimud.types.EffectType;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToEffectTypeConverter());
    }

    public static class StringToEffectTypeConverter implements Converter<String, EffectType> {
        @Override
        public EffectType convert(String source) {
            return EffectType.fromString(source);
        }
    }
}
