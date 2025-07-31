package com.tuempresa.cotizador.security.repository;

import com.tuempresa.cotizador.security.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);
    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:role IS NULL OR u.role = :role)")
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("role") com.tuempresa.cotizador.security.model.Role role,
                           Pageable pageable);

}
