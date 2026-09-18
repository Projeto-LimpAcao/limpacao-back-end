package com.limpacao.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.limpacao.dto.OfertaAjudaRequestDTO;
import com.limpacao.dto.OfertaAjudaResponseDTO;
import com.limpacao.service.OfertaAjudaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/relatorios/{relatorioId}/ofertas-ajuda")
@RequiredArgsConstructor
public class OfertaAjudaController {

    private final OfertaAjudaService service;

    @GetMapping
    public ResponseEntity<List<OfertaAjudaResponseDTO>> listar(@PathVariable Long relatorioId) {
        return ResponseEntity.ok(service.listarPorRelatorio(relatorioId));
    }

    @PostMapping
    public ResponseEntity<OfertaAjudaResponseDTO> salvar(@PathVariable Long relatorioId,
            @RequestBody OfertaAjudaRequestDTO dto) {
        OfertaAjudaRequestDTO request = new OfertaAjudaRequestDTO(relatorioId, dto.nome(), dto.telefone(), dto.mensagem());
        OfertaAjudaResponseDTO criado = service.salvar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
