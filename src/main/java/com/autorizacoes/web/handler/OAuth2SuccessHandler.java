package com.autorizacoes.web.handler;

import com.autorizacoes.core.model.User;
import com.autorizacoes.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    public OAuth2SuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        User domainUser = authService.fromOAuth2User(oauth2User);

        HttpSession session = request.getSession();
        session.setAttribute("currentUser", domainUser);

        response.sendRedirect("/home");
    }
}
