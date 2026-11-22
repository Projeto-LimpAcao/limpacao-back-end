package com.limpacao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.limpacao.entity.Usuario;
import com.limpacao.enums.PerfilUsuario;
import com.limpacao.repository.UsuarioRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AdministradorInicial implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ListaAdministradores listaAdministradores;
    private final String email;
    private final String senha;
    private final String nome;

    public AdministradorInicial(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            ListaAdministradores listaAdministradores,
            @Value("${limpacao.admin.email:${ADMIN_EMAIL:}}") String email,
            @Value("${limpacao.admin.senha:${ADMIN_SENHA:}}") String senha,
            @Value("${limpacao.admin.nome:${ADMIN_NOME:Administrador}}") String nome) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.listaAdministradores = listaAdministradores;
        this.email = email == null ? "" : email.trim();
        this.senha = senha;
        this.nome = nome;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        listaAdministradores.emails()
                .forEach(daLista -> usuarioRepository.findByEmail(daLista).ifPresent(this::promover));

        if (!email.isBlank()) {
            usuarioRepository.findByEmail(email).ifPresentOrElse(this::promover, this::criar);
        }
    }

    private void promover(Usuario usuario) {
        if (usuario.getPerfil() == PerfilUsuario.ADMIN) {
            return;
        }
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuarioRepository.save(usuario);
        log.info("Usuário {} promovido a administrador.", usuario.getEmail());
    }

    private void criar() {
        if (senha == null || senha.isBlank()) {
            log.warn("ADMIN_EMAIL definido sem ADMIN_SENHA: administrador não criado.");
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .perfil(PerfilUsuario.ADMIN)
                .build());
        log.info("Administrador {} criado.", email);
    }
}
