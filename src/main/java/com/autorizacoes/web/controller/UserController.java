package com.autorizacoes.web.controller;

import com.autorizacoes.core.model.UserBD;
import com.autorizacoes.core.security.SecurityLevel;
import com.autorizacoes.repository.UserDaoImp;
import com.autorizacoes.service.LoggerService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserDaoImp userRepository;

    public UserController(UserDaoImp userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listAll(Authentication authentication) {
        LoggerService.log("Listagem de todos os usuários solicitada por: "
                + authentication.getName());

        List<Map<String, Object>> result = userRepository.findAll()
                .stream()
                .map(this::toSafeMap)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/email")
    public ResponseEntity<?> findByEmail(@RequestParam @Email @Size(max=150) String value, Authentication authentication) {
        LoggerService.log("Busca por email solicitada por: " + authentication.getName()
                + " | email: " + value);

        Optional<UserBD> user = userRepository.findByEmail(value);

        return user
                .map(u -> ResponseEntity.ok(toSafeMap(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/role")
    public ResponseEntity<List<Map<String, Object>>> findByRole(@RequestParam SecurityLevel value, Authentication authentication) {
        LoggerService.log("Busca por role solicitada por: " + authentication.getName()
                + " | role: " + value);

        List<Map<String, Object>> result = userRepository.findByRole(value.name())
                .stream()
                .map(this::toSafeMap)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchByNome(@RequestParam @Size(max = 100) @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$") String nome, Authentication authentication) {
        LoggerService.log("Busca por nome solicitada por: " + authentication.getName()
                + " | nome: " + nome);

        List<Map<String, Object>> result = userRepository.findByNomeContaining(nome)
                .stream()
                .map(this::toSafeMap)
                .toList();

        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toSafeMap(UserBD u) {
        return Map.of(
                "id",       u.getId(),
                "nome",     u.getNome(),
                "email",    u.getEmail(),
                "role",     u.getRole(),
                "criadoEm", u.getCriadoEm() != null ? u.getCriadoEm().toString() : ""
        );
    } 
}