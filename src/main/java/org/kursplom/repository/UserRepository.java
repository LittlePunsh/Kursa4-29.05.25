package org.kursplom.repository;

import org.kursplom.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // Найти пользователя по имени

    Boolean existsByUsername(String username); // Проверить, существует ли пользователь с таким именем

    Boolean existsByEmail(String email); // Проверить, существует ли пользователь с таким email

}