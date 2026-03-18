package com.portrasdoclique.api.service;

import com.portrasdoclique.api.dto.MetricasDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseService {

    private final List<SseEmitter> clientes = new CopyOnWriteArrayList<>();

    public SseEmitter conectar() {
        SseEmitter emitter = new SseEmitter(300_000L);

        clientes.add(emitter);
        log.info("Novo cliente SSE conectado. Total: {}", clientes.size());

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (Exception e) {
            clientes.remove(emitter);
        }

        emitter.onCompletion(() -> {
            clientes.remove(emitter);
            log.info("Cliente SSE desconectado. Total: {}", clientes.size());
        });

        emitter.onTimeout(() -> {
            clientes.remove(emitter);
            log.info("Cliente SSE timeout. Total: {}", clientes.size());
        });

        emitter.onError(e -> {
            clientes.remove(emitter);
            log.info("Erro no cliente SSE. Total: {}", clientes.size());
        });

        return emitter;
    }

    public void emitirMetricas(MetricasDTO metricas) {
        List<SseEmitter> mortos = new CopyOnWriteArrayList<>();

        clientes.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("metricas")
                        .data(metricas));
            } catch (Exception e) {
                mortos.add(emitter);
            }
        });

        clientes.removeAll(mortos);
    }
}