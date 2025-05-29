package org.kursplom.service;

import org.kursplom.dto.SignUpRequest;
import org.kursplom.model.Role;
import org.kursplom.model.RoleName;
import org.kursplom.model.User;
import org.kursplom.repository.RoleRepository;
import org.kursplom.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerNewUser(SignUpRequest signUpRequest) {
        // Проверка на дублирование имени пользователя
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        // Проверка на дублирование email
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email Address already in use!");
        }

        // Создаем нового пользователя
        User user = new User(
                signUpRequest.getUsername(),
                passwordEncoder.encode(signUpRequest.getPassword()), // Хэшируем пароль!
                signUpRequest.getEmail());

        user.setCreatedAt(LocalDateTime.now()); // Устанавливаем дату создания

        // Назначаем роль по умолчанию (ROLE_USER)
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role USER not found."));

        roles.add(userRole);
        user.setRoles(roles);

        // Сохраняем пользователя в базу данных
        User savedUser = userRepository.save(user);

        return savedUser;
    }

}