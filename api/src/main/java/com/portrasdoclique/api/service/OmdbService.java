package com.portrasdoclique.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmdbService {

    @Value("${omdb.api.key}")
    private String apiKey;

    @Value("${omdb.base-url}")
    private String baseUrl;

    private final ObjectMapper objectMapper;

    public OmdbData buscarDetalhes(String titulo) {
        try {
            String url = baseUrl + "/?apikey=" + apiKey + "&t=" + titulo.replace(" ", "+");
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);

            if (!"True".equals(root.get("Response").asText())) {
                return OmdbData.empty();
            }

            String rottenTomatoes = null;
            JsonNode ratings = root.get("Ratings");
            if (ratings != null) {
                for (JsonNode rating : ratings) {
                    if ("Rotten Tomatoes".equals(rating.get("Source").asText())) {
                        rottenTomatoes = rating.get("Value").asText();
                        break;
                    }
                }
            }

            return OmdbData.builder()
                    .rottenTomatoes(rottenTomatoes)
                    .metacritic(root.path("Metascore").asText(null))
                    .premios(root.path("Awards").asText(null))
                    .build();

        } catch (Exception e) {
            log.error("Erro ao buscar detalhes no OMDb para: {}", titulo, e);
            return OmdbData.empty();
        }
    }

    public record OmdbData(String rottenTomatoes, String metacritic, String premios) {
        public static OmdbData empty() {
            return new OmdbData(null, null, null);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String rottenTomatoes;
            private String metacritic;
            private String premios;

            public Builder rottenTomatoes(String v) { this.rottenTomatoes = v; return this; }
            public Builder metacritic(String v) { this.metacritic = v; return this; }
            public Builder premios(String v) { this.premios = v; return this; }
            public OmdbData build() { return new OmdbData(rottenTomatoes, metacritic, premios); }
        }
    }
}