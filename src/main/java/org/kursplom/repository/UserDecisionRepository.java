package org.kursplom.repository;

import jakarta.transaction.Transactional;
import org.kursplom.model.Game;
import org.kursplom.model.UserDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDecisionRepository extends JpaRepository<UserDecision, Long> {
    Optional<UserDecision> findByUserIdAndDecisionPointId(Long userId, Long decisionPointId);
    List<UserDecision> findByUserIdAndDecisionPointIdIn(Long userId, List<Long> decisionPointIds);

    @Transactional
    void deleteByDecisionPointGame(Game game);

    List<UserDecision> findByUserIdOrderByDecisionPoint_OrderInGame(Long userId);

}
