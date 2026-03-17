package com.portrasdoclique.api.consumer;

import com.portrasdoclique.api.config.RabbitConfig;
import com.portrasdoclique.api.dto.MetricasDTO;
import com.portrasdoclique.api.event.FilmeBuscadoEvent;
import com.portrasdoclique.api.service.MetricasService;
import com.portrasdoclique.api.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricasConsumer {

    private final MetricasService metricasService;
    private final SseService sseService;

    @RabbitListener(queues = RabbitConfig.QUEUE_BUSCADO)
    public void consumir(FilmeBuscadoEvent event) {
        log.debug("Evento recebido: {} · {}ms · cache={}",
                event.getFilmeTitulo(), event.getTempoMs(), event.getCacheHit());

        metricasService.processar(event);

        MetricasDTO metricas = metricasService.getMetricas();
        sseService.emitirMetricas(metricas);
    }
}