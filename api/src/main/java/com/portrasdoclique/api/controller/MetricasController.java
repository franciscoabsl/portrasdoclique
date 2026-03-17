package com.portrasdoclique.api.controller;

import com.portrasdoclique.api.dto.MetricasDTO;
import com.portrasdoclique.api.service.MetricasService;
import com.portrasdoclique.api.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
public class MetricasController {

    private final MetricasService metricasService;
    private final SseService sseService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        log.debug("Novo cliente conectado ao stream de métricas");
        return sseService.conectar();
    }

    @GetMapping
    public MetricasDTO getMetricas() {
        return metricasService.getMetricas();
    }
}