package com.limpacao.config;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ListaAdministradores {

    private final Set<String> emails;

    public ListaAdministradores(@Value("${limpacao.admin.emails:}") String emails) {
        this.emails = Arrays.stream(emails.split(","))
                .map(email -> email.trim().toLowerCase(Locale.ROOT))
                .filter(email -> !email.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean ehAdmin(String email) {
        return email != null && emails.contains(email.trim().toLowerCase(Locale.ROOT));
    }

    public Set<String> emails() {
        return emails;
    }
}
