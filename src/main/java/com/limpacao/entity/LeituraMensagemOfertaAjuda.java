package com.limpacao.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "leituras_mensagem_oferta_ajuda",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_leitura_oferta_usuario",
                columnNames = { "oferta_id", "usuario_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeituraMensagemOfertaAjuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "oferta_id", nullable = false)
    private Long ofertaId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "lida_em", nullable = false)
    private LocalDateTime lidaEm;

    @Column(name = "ultima_mensagem_id")
    private Long ultimaMensagemId;
}
