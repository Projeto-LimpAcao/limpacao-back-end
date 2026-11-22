package com.limpacao.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ListaAdministradoresTest {

    @Test
    void reconheceEmailsDaListaSemDiferenciarMaiusculas() {
        var lista = new ListaAdministradores(" limpacao.projeto@gmail.com , outro@gmail.com ");

        assertTrue(lista.ehAdmin("limpacao.projeto@gmail.com"));
        assertTrue(lista.ehAdmin("Limpacao.Projeto@Gmail.com"));
        assertTrue(lista.ehAdmin(" outro@gmail.com "));
        assertFalse(lista.ehAdmin("ana@exemplo.com"));
        assertFalse(lista.ehAdmin(null));
    }

    @Test
    void listaVaziaNaoTemAdministradores() {
        var lista = new ListaAdministradores("");

        assertFalse(lista.ehAdmin("limpacao.projeto@gmail.com"));
        assertTrue(lista.emails().isEmpty());
    }
}
