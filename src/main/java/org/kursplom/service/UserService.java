package org.kursplom.service;

import jakarta.transaction.Transactional;
import org.kursplom.model.Role;
import org.kursplom.model.User;
import org.kursplom.repository.RoleRepository;
import org.kursplom.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }



    // Методы для AdminController:
    public long getUserCount() {
        return userRepository.count();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        logger.info("Attempting to update roles for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with id: " + userId));

        Set<Role> newRoles = new HashSet<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> rolesToAssign = roleRepository.findAllById(roleIds);
            if (rolesToAssign.size() != roleIds.size()) {
                List<Long> foundRoleIds = rolesToAssign.stream().map(Role::getId).collect(Collectors.toList());
                List<Long> notFoundIds = roleIds.stream().filter(id -> !foundRoleIds.contains(id)).collect(Collectors.toList());
                logger.warn("Some role IDs were not found during user role update for user {}. Not found IDs: {}", userId, notFoundIds);
            }
            newRoles.addAll(rolesToAssign);
        } else {
            logger.debug("No role IDs provided for user {}", userId);
        }

        user.setRoles(newRoles); // Установить новый набор ролей
        userRepository.save(user); // Сохранить пользователя
        logger.info("Roles updated for user {}. New roles: {}", userId, newRoles.stream().map(role -> role.getName().name()).collect(Collectors.joining(", ")));
    }

    // Удаление пользователя
    @Transactional
    public boolean deleteUser(Long userId) {
        logger.info("Attempting to delete user with ID: {}", userId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            userRepository.deleteById(userId); // Удаляем пользователя.
            logger.info("User with ID {} deleted successfully.", userId);
        } else {
            logger.warn("Attempted to delete non-existent user with ID: {}", userId);
            throw new UsernameNotFoundException("User Not Found with id: " + userId);
        }
        return false;
    }
}