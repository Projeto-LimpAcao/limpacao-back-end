package com.limpacao.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.limpacao.enums.StatusRelatorio;
import com.limpacao.enums.Urgencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "relatorios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Relatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, length = 255)
    private String localizacao;

    @Column(precision = 10, scale = 8)
    private Double latitude;

    @Column(precision = 11, scale = 8)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Urgencia urgencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusRelatorio status;

    @Column(nullable = false)
    private LocalDate data;

    @Column(columnDefinition = "TEXT")
    private String imagem;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 255)
    private String autor;

    @Builder.Default
    @Column(nullable = false)
    private Integer participantes = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer ajudantes = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean multirao = false;

    @Column(name = "status_anterior", length = 30)
    private String statusAnterior;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusRelatorio.PENDING;
        }
        if (this.data == null) {
            this.data = LocalDate.now();
        }
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
