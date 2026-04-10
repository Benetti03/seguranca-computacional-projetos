package com.autorizacoes.service;

import com.autorizacoes.core.model.User;
import com.autorizacoes.core.security.SecurityLevel;

public class AccessControlService {

    private AccessControlService() {}

    // No Read Up
    public static boolean canRead(User user, SecurityLevel resourceLevel) {
        boolean allowed = user.getSecurityLevel().getLevel() >= resourceLevel.getLevel();

        LoggerService.log(
                user.getUsername() + " tentando LER recurso " + resourceLevel +
                        " => " + (allowed ? "PERMITIDO" : "NEGADO")
        );

        return allowed;
    }

    // No Write Down
    public static boolean canWrite(User user, SecurityLevel resourceLevel) {
        boolean allowed = user.getSecurityLevel().getLevel() <= resourceLevel.getLevel();

        LoggerService.log(
                user.getUsername() + " tentando ESCREVER em recurso " + resourceLevel +
                        " => " + (allowed ? "PERMITIDO" : "NEGADO")
        );

        return allowed;
    }
}
