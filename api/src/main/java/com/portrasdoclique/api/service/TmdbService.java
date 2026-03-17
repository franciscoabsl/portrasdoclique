package com.portrasdoclique.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portrasdoclique.api.dto.FilmeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.base-url}")
    private String baseUrl;

    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    public FilmeDTO buscarFilmeAleatorio() {
        try {
            String url = baseUrl + "/movie/popular?api_key=" + apiKey + "&language=pt-BR&page=1";
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.get("results");

            int index = random.nextInt(results.size());
            JsonNode filme = results.get(index);

            String posterPath = filme.get("poster_path").asText();

            return FilmeDTO.builder()
                    .id(filme.get("id").asInt())
                    .titulo(filme.get("title").asText())
                    .tituloOriginal(filme.get("original_title").asText())
                    .sinopse(filme.get("overview").asText())
                    .posterPath(posterPath)
                    .posterUrl("https://image.tmdb.org/t/p/w500" + posterPath)
                    .nota(filme.get("vote_average").asDouble())
                    .dataLancamento(filme.get("release_date").asText())
                    .build();

        } catch (Exception e) {
            log.error("Erro ao buscar filme no TMDB", e);
            throw new RuntimeException("TMDB indisponível", e);
        }
    }
}