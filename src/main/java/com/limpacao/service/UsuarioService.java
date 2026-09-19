package com.limpacao.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.limpacao.dto.UsuarioRequestDTO;
import com.limpacao.dto.UsuarioResponseDTO;
import com.limpacao.mapper.UsuarioMapper;
import com.limpacao.entity.Usuario;
import com.limpacao.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return mapper.toDTOList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario nao encontrado com id: " + id));
        return mapper.toDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO salvar(UsuarioRequestDTO dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email ja cadastrado");
        }
        Usuario usuario = mapper.toEntity(dto);
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        return mapper.toDTO(repository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario existente = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario nao encontrado com id: " + id));
        existente.setNome(dto.nome());
        existente.setEmail(dto.email());
        existente.setSenha(passwordEncoder.encode(dto.senha()));
        return mapper.toDTO(repository.save(existente));
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Usuario nao encontrado com id: " + id);
        }
        repository.deleteById(id);
    }
}
