package org.kursplom.service;

import org.kursplom.model.*;
import org.kursplom.repository.*;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DecisionService {

    private static final Logger logger = LoggerFactory.getLogger(DecisionService.class);

    private final DecisionPointRepository decisionPointRepository;
    private final DecisionChoiceRepository decisionChoiceRepository;
    private final UserDecisionRepository userDecisionRepository;
    private final UserRepository userRepository;

    private final GameRepository gameRepository;
    private final DecisionTypeRepository decisionTypeRepository;


    public DecisionService(DecisionPointRepository decisionPointRepository,
                           DecisionChoiceRepository decisionChoiceRepository,
                           UserDecisionRepository userDecisionRepository,
                           UserRepository userRepository,
                           GameRepository gameRepository,
                           DecisionTypeRepository decisionTypeRepository) {
        this.decisionPointRepository = decisionPointRepository;
        this.decisionChoiceRepository = decisionChoiceRepository;
        this.userDecisionRepository = userDecisionRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.decisionTypeRepository = decisionTypeRepository;

        //Логирование для проверки внедрения репозиториев
        if (this.decisionPointRepository == null) logger.error("DecisionService: DecisionPointRepository was not injected!");
        if (this.decisionChoiceRepository == null) logger.error("DecisionService: DecisionChoiceRepository was not injected!");
        if (this.userDecisionRepository == null) logger.error("DecisionService: UserDecisionRepository was not injected!");
        if (this.userRepository == null) logger.error("DecisionService: UserRepository was not injected!");
        if (this.gameRepository == null) logger.error("DecisionService: GameRepository was not injected!");
        if (this.decisionTypeRepository == null) logger.error("DecisionService: DecisionTypeRepository was not injected!");
    }

    public List<String> getChaptersWithDecisionsByGameOrdered(Long gameId) {
        logger.debug("Fetching ordered chapter names for game ID: {}", gameId);
        List<DecisionPoint> points = decisionPointRepository.findByGameIdOrderByOrderInGame(gameId);
        return points.stream()
                .map(DecisionPoint::getChapterName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
    public List<String> getQuestsWithDecisionsByGameAndChapterOrdered(Long gameId, String chapterName) {
        logger.debug("Fetching ordered quest names for game ID: {} and chapter: {}", gameId, chapterName);
        List<DecisionPoint> points = decisionPointRepository.findByGameIdAndChapterNameOrderByOrderInGame(gameId, chapterName);
        return points.stream()
                .map(DecisionPoint::getQuestName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
    public List<DecisionPoint> getDecisionPointsByGameAndChapterOrdered(Long gameId, String chapterName) {
        logger.debug("Fetching ordered decision points for game ID: {} and chapter: {}", gameId, chapterName);
        List<DecisionPoint> points = decisionPointRepository.findByGameIdAndChapterNameOrderByOrderInGame(gameId, chapterName);
        logger.debug("Found {} decision points for game {} and chapter {}", points.size(), gameId, chapterName);
        return points;
    }
    public List<DecisionPoint> getDecisionPointsByGameAndChapterAndQuest(Long gameId, String chapterName, String questName) {
        logger.debug("Fetching ordered decision points for game ID: {}, chapter: {}, and quest: {}", gameId, chapterName, questName);
        List<DecisionPoint> points = decisionPointRepository.findByGameIdAndChapterNameAndQuestNameOrderByOrderInGame(gameId, chapterName, questName);
        logger.debug("Found {} decision points for game {}, chapter {}, quest {}", points.size(), gameId, chapterName, questName);
        return points;
    }
    public Optional<DecisionPoint> getDecisionPointById(Long pointId) {
        logger.debug("Fetching decision point by ID: {}", pointId);
        return decisionPointRepository.findById(pointId);
    }
    public Optional<UserDecision> getUserDecisionForPoint(Long userId, Long pointId) {
        logger.debug("Fetching user decision for user ID: {} and point ID: {}", userId, pointId);
        return userDecisionRepository.findByUserIdAndDecisionPointId(userId, pointId);
    }
    public List<UserDecision> getUserDecisionsForPoints(Long userId, List<DecisionPoint> points) {
        logger.debug("Fetching user decisions for user ID: {} and {} points.", userId, points != null ? points.size() : 0);
        if (points == null || points.isEmpty()) {
            return List.of();
        }
        List<Long> pointIds = points.stream()
                .map(DecisionPoint::getId)
                .collect(Collectors.toList());
        List<UserDecision> decisions = userDecisionRepository.findByUserIdAndDecisionPointIdIn(userId, pointIds);
        logger.debug("Found {} user decisions for user {} among {} points.", decisions.size(), userId, points.size());
        return decisions;
    }
    @Transactional
    public UserDecision saveUserDecision(Long userId, Long pointId, Long chosenChoiceId) {
        logger.info("Attempting to save decision for user ID: {}, point ID: {}, chosen choice ID: {}", userId, pointId, chosenChoiceId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found with id: " + userId));

        DecisionPoint decisionPoint = decisionPointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException("Decision Point not found with ID: " + pointId));

        DecisionChoice chosenChoice = decisionChoiceRepository.findById(chosenChoiceId)
                .orElseThrow(() -> new RuntimeException("Decision Choice not found with ID: " + chosenChoiceId));

        if (!chosenChoice.getDecisionPoint().getId().equals(pointId)) {
            logger.error("Attempted to save choice {} for point {} - choice does not belong to point.", chosenChoiceId, pointId);
            throw new IllegalArgumentException("Choice ID " + chosenChoiceId + " does not belong to Decision Point ID " + pointId);
        }

        Optional<UserDecision> existingDecision = userDecisionRepository.findByUserIdAndDecisionPointId(userId, pointId);

        UserDecision userDecision;
        if (existingDecision.isPresent()) {
            userDecision = existingDecision.get();
            userDecision.setChosenChoice(chosenChoice);
            userDecision.setCreatedAt(LocalDateTime.now());
            logger.info("Updating existing decision {} for user {} and point {}", userDecision.getId(), userId, pointId);
        } else {
            userDecision = new UserDecision();
            userDecision.setUser(user);
            userDecision.setDecisionPoint(decisionPoint);
            userDecision.setChosenChoice(chosenChoice);
            userDecision.setCreatedAt(LocalDateTime.now());
            logger.info("Creating new decision for user {} and point {}", userId, pointId);
        }

        UserDecision savedDecision = userDecisionRepository.save(userDecision); // Сохранить/обновить
        logger.info("Decision saved successfully. Decision ID: {}", savedDecision.getId());
        return savedDecision;
    }


    // Методы для AdminController (управление контентом):
    public long getDecisionPointCount() {
        return decisionPointRepository.count();
    }

    @Transactional
    public Optional<DecisionPoint> updateDecisionPoint(Long pointId, DecisionPoint updatedPointData, Long gameId, Long decisionTypeId) {
        logger.info("Attempting to update DecisionPoint with ID: {}", pointId);
        Optional<DecisionPoint> existingPointOptional = decisionPointRepository.findById(pointId);

        if (existingPointOptional.isPresent()) {
            DecisionPoint existingPoint = existingPointOptional.get();

            // Находим сущности Game и DecisionType по ID для связывания
            Game game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));
            DecisionType type = decisionTypeRepository.findById(decisionTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Decision type not found with ID: " + decisionTypeId));


            // Обновляем поля из предоставленных данных
            existingPoint.setGame(game);
            existingPoint.setDecisionType(type);
            existingPoint.setChapterName(updatedPointData.getChapterName());
            existingPoint.setQuestName(updatedPointData.getQuestName());
            existingPoint.setIdentifier(updatedPointData.getIdentifier());
            existingPoint.setDescription(updatedPointData.getDescription());
            existingPoint.setOrderInGame(updatedPointData.getOrderInGame());

            DecisionPoint savedPoint = decisionPointRepository.save(existingPoint); // Сохраняем обновленную точку
            logger.info("DecisionPoint with ID {} updated successfully.", savedPoint.getId());
            return Optional.of(savedPoint);
        } else {
            logger.warn("DecisionPoint with ID {} not found for update.", pointId);
            return Optional.empty();
        }
    }

    // Удаление точки решения
    @Transactional
    public boolean deleteDecisionPoint(Long pointId) {
        logger.info("Attempting to delete DecisionPoint with ID: {}", pointId);
        Optional<DecisionPoint> pointOptional = decisionPointRepository.findById(pointId);

        if (pointOptional.isPresent()) {
            DecisionPoint pointToDelete = pointOptional.get();

            decisionPointRepository.delete(pointToDelete);
            logger.info("DecisionPoint with ID {} deleted successfully.", pointId);
            return true;
        } else {
            logger.warn("DecisionPoint with ID {} not found for deletion.", pointId);
            return false;
        }
    }


    // Обновление варианта выбора
    @Transactional
    public Optional<DecisionChoice> updateDecisionChoice(Long choiceId, DecisionChoice updatedChoiceData) {
        logger.info("Attempting to update DecisionChoice with ID: {}", choiceId);
        Optional<DecisionChoice> existingChoiceOptional = decisionChoiceRepository.findById(choiceId);

        if (existingChoiceOptional.isPresent()) {
            DecisionChoice existingChoice = existingChoiceOptional.get();

            // Обновляем поля из предоставленных данных
            existingChoice.setIdentifier(updatedChoiceData.getIdentifier());
            existingChoice.setDescription(updatedChoiceData.getDescription());
            existingChoice.setConsequenceText(updatedChoiceData.getConsequenceText());

            DecisionChoice savedChoice = decisionChoiceRepository.save(existingChoice); // Сохраняем обновленный вариант
            logger.info("DecisionChoice with ID {} updated successfully.", savedChoice.getId());
            return Optional.of(savedChoice);
        } else {
            logger.warn("DecisionChoice with ID {} not found for update.", choiceId);
            return Optional.empty();
        }
    }

    // Удаление варианта выбора
    @Transactional
    public boolean deleteDecisionChoice(Long choiceId) {
        logger.info("Attempting to delete DecisionChoice with ID: {}", choiceId);
        Optional<DecisionChoice> choiceOptional = decisionChoiceRepository.findById(choiceId);

        if (choiceOptional.isPresent()) {
            DecisionChoice choiceToDelete = choiceOptional.get();

            decisionChoiceRepository.delete(choiceToDelete); // Удаляем сам вариант
            logger.info("DecisionChoice with ID {} deleted successfully.", choiceId);
            return true;
        } else {
            logger.warn("DecisionChoice with ID {} not found for deletion.", choiceId);
            return false;
        }
    }


    // Метод для отображения всех решений пользователя (для админа)
    public List<UserDecision> getAllUserDecisions(Long userId) {
        logger.debug("Fetching all decisions for user ID: {}", userId);
        List<UserDecision> decisions = userDecisionRepository.findByUserIdOrderByDecisionPoint_OrderInGame(userId);
        logger.debug("Found {} decisions for user ID: {}", decisions.size(), userId);
        return decisions;
    }


    public Optional<DecisionChoice> getDecisionChoiceById(Long choiceId) {
        logger.debug("Fetching decision choice by ID: {}", choiceId);
        if (decisionChoiceRepository == null) {
            logger.error("DecisionService: DecisionChoiceRepository is null when calling findById!");
            throw new RuntimeException("DecisionChoiceRepository is not initialized in DecisionService.");
        }

        Optional<DecisionChoice> result = decisionChoiceRepository.findById(choiceId);
        logger.debug("DecisionService: findById({}) returned: {}", choiceId, result.isPresent() ? "Optional with value" : "Empty Optional");

        return result;
    }

}