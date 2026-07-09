package com.espe.product;

import com.espe.product.dto.RegistroUsuarioRequest;
import com.espe.product.dto.UsuarioResponse;
import com.espe.product.entity.Role;
import com.espe.product.entity.Usuario;
import com.espe.product.repository.UsuarioRepository;
import com.espe.product.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    private UsuarioRepository repository;
    private PasswordEncoder passwordEncoder;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UsuarioService(repository, passwordEncoder);
    }

    @Test
    void debeRegistrarUsuarioExitosamente() {
        RegistroUsuarioRequest request = new RegistroUsuarioRequest("testuser", "password123");
        when(repository.existsByUsernameIgnoreCase("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setId(1L);
        usuarioGuardado.setUsername("testuser");
        usuarioGuardado.setPasswordHash("encodedPassword");
        usuarioGuardado.setRole(Role.USER);
        usuarioGuardado.setEnabled(true);

        when(repository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        UsuarioResponse response = service.registrar(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("testuser", response.username());
        assertEquals(Role.USER, response.role());
        assertTrue(response.enabled());

        verify(repository).existsByUsernameIgnoreCase("testuser");
        verify(passwordEncoder).encode("password123");
        verify(repository).save(any(Usuario.class));
    }

    @Test
    void debeLanzarExceptionSiUsuarioYaExiste() {
        RegistroUsuarioRequest request = new RegistroUsuarioRequest("existinguser", "password123");
        when(repository.existsByUsernameIgnoreCase("existinguser")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.registrar(request));

        verify(repository).existsByUsernameIgnoreCase("existinguser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any(Usuario.class));
    }
}
