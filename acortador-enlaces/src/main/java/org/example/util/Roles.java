package org.example.util;

import io.javalin.core.security.Role;

public enum Roles implements Role {
    DESCONOCIDO,
    AUTENTICADO,
    ROLE_USUARIO,
    ROLE_ADMIN
}
