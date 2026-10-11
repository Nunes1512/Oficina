package com.oficina.oficina_api.dto;

public record LoginResponse(
        String token,
        String tipo,
        String login,
        String role
) {}
