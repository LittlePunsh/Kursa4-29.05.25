package org.kursplom.repository;
import jakarta.transaction.Transactional;
import org.kursplom.model.Game;
import org.kursplom.model.SaveConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SaveConfigurationRepository extends JpaRepository<SaveConfiguration, Long> {
    Optional<SaveConfiguration> findByName(String name);

    @Transactional
    void deleteByOriginatingGame(Game game);
}