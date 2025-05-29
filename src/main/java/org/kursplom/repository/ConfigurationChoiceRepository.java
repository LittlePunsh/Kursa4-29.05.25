package org.kursplom.repository;

import jakarta.transaction.Transactional;
import org.kursplom.model.ConfigurationChoice;
import org.kursplom.model.Game;
import org.kursplom.model.SaveConfiguration;
import org.kursplom.model.DecisionPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationChoiceRepository extends JpaRepository<ConfigurationChoice, Long> {
    boolean existsByConfigurationAndDecisionPoint(SaveConfiguration configuration, DecisionPoint decisionPoint);

    @Transactional
    void deleteByConfigurationOriginatingGame(Game game);
}