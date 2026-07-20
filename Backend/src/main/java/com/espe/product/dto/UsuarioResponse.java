package com.espe.product.dto;

import com.espe.product.entity.Role;

public record UsuarioResponse(
        Long id,
        String username,
        Role role,
        Boolean enabled
) {}
