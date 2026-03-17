package com.portrasdoclique.api.controller;

import com.portrasdoclique.api.service.FluxoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/fluxo")
@RequiredArgsConstructor
public class FluxoController {

    private final FluxoService fluxoService;

    @GetMapping(value = "/iniciar", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter iniciar(
            @RequestParam(defaultValue = "false") boolean chaos,
            HttpServletRequest request) {

        String ip = obterIp(request);
        log.debug("Fluxo iniciado · ip={} · chaos={}", ip, chaos);

        SseEmitter emitter = new SseEmitter(60_000L);

        CompletableFuture.runAsync(() ->
                fluxoService.executar(emitter, ip, chaos)
        );

        return emitter;
    }

    private String obterIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}