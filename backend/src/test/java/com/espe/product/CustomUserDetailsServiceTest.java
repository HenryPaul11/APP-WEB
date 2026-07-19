package com.espe.product;

import com.espe.product.entity.Role;
import com.espe.product.entity.Usuario;
import com.espe.product.repository.UsuarioRepository;
import com.espe.product.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private UsuarioRepository repository;
    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        service = new CustomUserDetailsService(repository);
    }

    @Test
    void debeCargarUsuarioPorUsernameExitosamente() {
        Usuario usuario = new Usuario();
        usuario.setUsername("testuser");
        usuario.setPasswordHash("hashedpassword");
        usuario.setRole(Role.USER);
        usuario.setEnabled(true);

        when(repository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = service.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("hashedpassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        verify(repository).findByUsernameIgnoreCase("testuser");
    }

    @Test
    void debeLanzarUsernameNotFoundExceptionSiNoExiste() {
        when(repository.findByUsernameIgnoreCase("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("unknown"));

        verify(repository).findByUsernameIgnoreCase("unknown");
    }
}
