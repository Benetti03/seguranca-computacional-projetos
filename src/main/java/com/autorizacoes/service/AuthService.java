package com.autorizacoes.service;

import com.autorizacoes.core.model.User;
import com.autorizacoes.core.security.SecurityLevel;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private static final Map<String, SecurityLevel> EMAIL_LEVEL_MAP = Map.of(
            "lb.benetti@hotmail.com", SecurityLevel.SECRET,
            "gerente@gmail.com", SecurityLevel.CONFIDENTIAL,
            "lorenzobenetti@alunos.utfpr.edu.br", SecurityLevel.PUBLIC
    );

    public User fromOAuth2User(OAuth2User oauth2User) {
        String email    = oauth2User.getAttribute("email");
        String name     = oauth2User.getAttribute("name");
        SecurityLevel level = resolveLevel(email);

        LoggerService.log("Login com o OAuth2.0 ocorreu com sucesso: " + email + " | Nível: " + level);

        return new User(name, email, level);
    }

    public SecurityLevel resolveLevel(String email) {
        return EMAIL_LEVEL_MAP.getOrDefault(email, SecurityLevel.PUBLIC);
    }
}