package org.kursplom.repository;

import jakarta.transaction.Transactional;
import org.kursplom.model.PremadeSave;
import org.kursplom.model.SaveConfiguration;
import org.kursplom.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PremadeSaveRepository extends JpaRepository<PremadeSave, Long> {
    boolean existsByConfigurationAndGame(SaveConfiguration configuration, Game game);
    Optional<PremadeSave> findByConfigurationAndGame(SaveConfiguration configuration, Game game);

    @Transactional
    void deleteByGame(Game game);
}