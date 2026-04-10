package com.autorizacoes.core.model;

import com.autorizacoes.core.security.SecurityLevel;

public class User {
    private final String username;
    private final String email;       // vem do token OAuth2 do Google
    private final SecurityLevel securityLevel;

    public User(String username, String email, SecurityLevel securityLevel) {
        this.username = username;
        this.email = email;
        this.securityLevel = securityLevel;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public SecurityLevel getSecurityLevel() {
        return securityLevel;
    }
}