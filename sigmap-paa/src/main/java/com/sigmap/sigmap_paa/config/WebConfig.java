package com.sigmap.sigmap_paa.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("json", MediaType.APPLICATION_JSON);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // Add support for application/json;charset=UTF-8
        MediaType applicationJsonUtf8 = new MediaType(
                MediaType.APPLICATION_JSON.getType(),
                MediaType.APPLICATION_JSON.getSubtype(),
                StandardCharsets.UTF_8
        );
        converter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                applicationJsonUtf8
        ));
        converters.add(converter);
    }

    @Bean
    public LocaleResolver localeResolver() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        AcceptHeaderLocaleResolver localeResolver =
                new AcceptHeaderLocaleResolver() {
                    @Override
                    public Locale resolveLocale(HttpServletRequest request) {
                        String acceptLanguage = request.getHeader("Accept-Language");
                        if (acceptLanguage == null) {
                            return org.springframework.util.StringUtils.parseLocale("en");
                        }
                        if (acceptLanguage.contains(";")) {
                            acceptLanguage = acceptLanguage.substring(0, acceptLanguage.indexOf(";"));
                        }
                        if (acceptLanguage.contains(",")) {
                            acceptLanguage = acceptLanguage.substring(0, acceptLanguage.indexOf(","));
                        }
                        return org.springframework.util.StringUtils.parseLocale(acceptLanguage);
                    }

                    

                    @Override
                    public void setLocale(
                            HttpServletRequest request, HttpServletResponse response, Locale locale) {
                        if (locale == null) {
                            LocaleContextHolder.setLocale(Locale.ENGLISH);
                        }
                        else {
                            LocaleContextHolder.setLocale(locale);
                        }
                    }
                };
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }
}