package org.kursplom.repository;

import org.kursplom.model.DecisionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DecisionTypeRepository extends JpaRepository<DecisionType, Long> {
    Optional<DecisionType> findByName(String name);
}