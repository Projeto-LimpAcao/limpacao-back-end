package com.limpacao.service;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.limpacao.dto.PreferenciasUsuarioRequestDTO;
import com.limpacao.dto.PreferenciasUsuarioResponseDTO;
import com.limpacao.entity.Usuario;
import com.limpacao.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreferenciasUsuarioService {

    private static final Set<String> PALETAS_PERMITIDAS = Set.of(
            "verde", "azul", "roxo", "laranja", "rosa", "preto");

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public PreferenciasUsuarioResponseDTO buscar(String email) {
        Usuario usuario = buscarUsuario(email);
        return mapear(usuario);
    }

    @Transactional
    public PreferenciasUsuarioResponseDTO atualizar(
            String email,
            PreferenciasUsuarioRequestDTO preferencias) {
        if (preferencias == null || !PALETAS_PERMITIDAS.contains(preferencias.paleta())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paleta de cores inválida.");
        }

        Usuario usuario = buscarUsuario(email);
        usuario.setPaletaTema(preferencias.paleta());
        usuario.setModoEscuro(preferencias.modoEscuro());
        return mapear(usuarioRepository.save(usuario));
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário autenticado não encontrado."));
    }

    private PreferenciasUsuarioResponseDTO mapear(Usuario usuario) {
        String paleta = PALETAS_PERMITIDAS.contains(usuario.getPaletaTema())
                ? usuario.getPaletaTema()
                : "verde";
        return new PreferenciasUsuarioResponseDTO(paleta, usuario.isModoEscuro());
    }
}
