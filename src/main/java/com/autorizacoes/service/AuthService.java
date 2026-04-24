package com.autorizacoes.service;

import com.autorizacoes.core.model.User;
import com.autorizacoes.core.model.UserBD;
import com.autorizacoes.core.security.SecurityLevel;
import com.autorizacoes.repository.UserDaoImp;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    /*private static final Map<String, SecurityLevel> EMAIL_LEVEL_MAP = Map.of(
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
    }*/

    private final UserDaoImp userRepository;

    public AuthService(UserDaoImp userRepository) {
        this.userRepository = userRepository;
    }

    public User fromOAuth2User(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String name  = oauth2User.getAttribute("name");

        UserBD user = findOrCreate(email, name);
        SecurityLevel level = SecurityLevel.valueOf(user.getRole());

        LoggerService.log("Login OAuth2 bem-sucedido: " + email + " | Nível: " + level);

        return new User(name, email, level);
    }

    private UserBD findOrCreate(String email, String name) {
        Optional<UserBD> emailGetter = userRepository.findByEmail(email);

        if (emailGetter.isPresent()) {
            return emailGetter.get();
        }

        UserBD newUser = new UserBD(name, email, "", SecurityLevel.PUBLIC.name());
        UserBD saved = userRepository.save(newUser);

        LoggerService.log("Novo usuário registrado automaticamente: " + email);
        return saved;
    }

    public SecurityLevel resolveLevel(String email) {
        return userRepository.findByEmail(email)
                .map(u -> SecurityLevel.valueOf(u.getRole()))
                .orElse(SecurityLevel.PUBLIC);
    }
}