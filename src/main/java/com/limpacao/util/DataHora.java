package com.limpacao.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/*
 * As datas são gravadas como LocalDateTime, no fuso do servidor (UTC, ver
 * LimpacaoApplication). Para a resposta da API elas saem com o fuso junto
 * (ex.: 2026-11-11T02:02:03Z), assim o navegador converte para o fuso de
 * quem está usando o sistema.
 */
public final class DataHora {

    private DataHora() {
    }

    public static OffsetDateTime comFuso(LocalDateTime data) {
        return data == null ? null : data.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
