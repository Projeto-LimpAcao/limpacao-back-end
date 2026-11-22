package com.limpacao.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.limpacao.dto.MetricasRequisicoesDTO;
import com.limpacao.service.MetricasRequisicoesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
public class MetricasController {

    private final MetricasRequisicoesService service;

    @GetMapping("/requisicoes")
    public ResponseEntity<MetricasRequisicoesDTO> requisicoes() {
        return ResponseEntity.ok(service.resumo());
    }
}
