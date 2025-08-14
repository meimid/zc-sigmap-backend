package com.sigmap.common.adresse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigmap.common.adresse.request.AdresseRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class StringToAdresseRequestConverter implements Converter<String, AdresseRequest> {

    // Inject Jackson's ObjectMapper to handle JSON parsing
    private final ObjectMapper objectMapper;

    @Override
    public AdresseRequest convert(@NonNull String source) {
        // If the source string is empty or null, return null.
        if (source.isEmpty()) {
            return null;
        }
        try {
            // Use the ObjectMapper to read the JSON string and create an AdresseRequest object.
            return objectMapper.readValue(source, AdresseRequest.class);
        } catch (IOException e) {
            // If the JSON is malformed, you could throw a custom exception or log the error.
            // For now, we'll re-throw it as a runtime exception.
            throw new IllegalArgumentException("Failed to convert JSON string to AdresseRequest", e);
        }
    }
}
