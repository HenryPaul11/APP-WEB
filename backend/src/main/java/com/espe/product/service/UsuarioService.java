package com.espe.product.service;

import com.espe.product.dto.RegistroUsuarioRequest;
import com.espe.product.dto.UsuarioResponse;
import com.espe.product.entity.Role;
import com.espe.product.entity.Usuario;
import com.espe.product.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroUsuarioRequest request) {
        if (repository.existsByUsernameIgnoreCase(request.username())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El nombre de usuario ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRole(Role.USER);
        usuario.setEnabled(true);

        Usuario guardado = repository.save(usuario);

        return new UsuarioResponse(
                guardado.getId(),
                guardado.getUsername(),
                guardado.getRole(),
                guardado.getEnabled()
        );
    }
}
