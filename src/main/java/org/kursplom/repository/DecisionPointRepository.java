package org.kursplom.repository;

import org.kursplom.model.DecisionPoint;
import org.kursplom.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.transaction.Transactional;
import java.util.List;

public interface DecisionPointRepository extends JpaRepository<DecisionPoint, Long> {
    List<DecisionPoint> findByGameIdOrderByOrderInGame(Long gameId);
    List<DecisionPoint> findByGameIdAndChapterNameOrderByOrderInGame(Long gameId, String chapterName);
    List<DecisionPoint> findByGameIdAndChapterNameAndQuestNameOrderByOrderInGame(Long gameId, String chapterName, String questName);

    @Transactional
    void deleteByGame(Game game);

    long countByGame(Game game);

}