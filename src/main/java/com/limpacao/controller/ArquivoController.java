package com.limpacao.controller;

import java.time.Duration;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.limpacao.exception.RecursoNaoEncontradoException;
import com.limpacao.service.ArmazenamentoImagemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/arquivos")
@RequiredArgsConstructor
public class ArquivoController {

    private final ArmazenamentoImagemService armazenamento;

    /* Cada arquivo tem nome único (UUID) e nunca muda, então pode ficar em cache. */
    @GetMapping("/{nome:.+}")
    public ResponseEntity<Resource> baixar(@PathVariable String nome) {
        var arquivo = armazenamento.localizar(nome)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Arquivo não encontrado."));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(armazenamento.tipoConteudo(nome)))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .body(new FileSystemResource(arquivo));
    }
}
