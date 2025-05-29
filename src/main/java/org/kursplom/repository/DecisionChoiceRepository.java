package org.kursplom.repository;

import org.kursplom.model.DecisionChoice;
import org.kursplom.model.DecisionPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface DecisionChoiceRepository extends JpaRepository<DecisionChoice, Long> {
    List<DecisionChoice> findByDecisionPointIn(List<DecisionPoint> decisionPoints);
}