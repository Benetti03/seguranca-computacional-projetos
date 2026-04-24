package com.autorizacoes.repository;

import com.autorizacoes.core.model.UserBD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDaoImp extends JpaRepository<UserBD, Integer> {

    Optional<UserBD> findByEmail(String email);

    @Query("SELECT u FROM UserBD u WHERE u.role = :role ORDER BY u.criadoEm DESC")
    List<UserBD> findByRole(@Param("role") String role);

    @Query("SELECT u FROM UserBD u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<UserBD> findByNomeContaining(@Param("nome") String nome);

    boolean existsByEmail(String email);
}
