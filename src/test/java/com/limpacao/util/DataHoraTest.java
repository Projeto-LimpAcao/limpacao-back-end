package com.limpacao.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;

import com.limpacao.dto.MensagemChatResponseDTO;

import tools.jackson.databind.json.JsonMapper;

class DataHoraTest {

    private final TimeZone fusoOriginal = TimeZone.getDefault();

    @AfterEach
    void restaurarFuso() {
        TimeZone.setDefault(fusoOriginal);
    }

    @Test
    void incluiOFusoDoServidorNaData() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("America/Sao_Paulo")));

        var data = DataHora.comFuso(LocalDateTime.of(2026, 11, 10, 23, 2, 3));

        assertEquals("2026-11-10T23:02:03-03:00", data.toString());
        assertEquals("2026-11-11T02:02:03Z", data.toInstant().toString());
    }

    @Test
    void jsonDaApiSaiComOFuso() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("America/Sao_Paulo")));

        String json = JsonMapper.builder().build()
                .writeValueAsString(DataHora.comFuso(LocalDateTime.of(2026, 11, 10, 23, 2, 3)));

        assertEquals("\"2026-11-10T23:02:03-03:00\"", json);
    }

    @Test
    void mensagemDoChatPeloWebSocketSaiComOFuso() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("America/Sao_Paulo")));
        var mensagem = new MensagemChatResponseDTO(
                1L, 1L, "Ana", "Oi", DataHora.comFuso(LocalDateTime.of(2026, 11, 10, 23, 2, 3)),
                null, null, null);

        var convertida = new JacksonJsonMessageConverter().toMessage(mensagem, null);
        String json = new String((byte[]) convertida.getPayload(), StandardCharsets.UTF_8);

        assertTrue(json.contains("\"criadoEm\":\"2026-11-10T23:02:03-03:00\""), json);
    }

    @Test
    void servidorEmUtcEnviaOInstanteCorreto() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")));

        var data = DataHora.comFuso(LocalDateTime.of(2026, 11, 11, 2, 2, 3));

        assertEquals("2026-11-11T02:02:03Z", data.toString());
        assertEquals(
                "2026-11-10T23:02:03",
                data.atZoneSameInstant(ZoneId.of("America/Sao_Paulo")).toLocalDateTime().toString());
    }

    @Test
    void mantemNulo() {
        assertNull(DataHora.comFuso(null));
    }
}
