package com.limpacao.entity;

import java.time.LocalDateTime;

import com.limpacao.enums.TipoParticipacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "participacoes_relatorio", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"relatorio_id", "usuario_id", "tipo_participacao"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "relatorio_id", nullable = false)
    private Long relatorioId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_participacao", nullable = false, length = 30)
    private TipoParticipacao tipoParticipacao;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }
}
