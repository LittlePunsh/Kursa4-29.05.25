package org.kursplom.controller;

import org.kursplom.model.DecisionPoint;
import org.kursplom.model.User;
import org.kursplom.model.UserDecision;
import org.kursplom.service.DecisionService;
import org.kursplom.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Controller
@RequestMapping("/user/decisions")
public class UserDecisionsController {

    private static final Logger logger = LoggerFactory.getLogger(UserDecisionsController.class);

    private final DecisionService decisionService;
    private final UserService userService;

    public UserDecisionsController(DecisionService decisionService, UserService userService) {
        this.decisionService = decisionService;
        this.userService = userService;
    }

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Authenticated user details not available");
        }
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found in DB with username: " + userDetails.getUsername()));
    }

    // Страница 1 - Список Глав для игры
    // URL: /user/decisions/{gameId}
    // Шаблон: chapters_list.html
    @GetMapping("/{gameId}")
    public String listChapters(@PathVariable Long gameId, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<String> chapterNames = decisionService.getChaptersWithDecisionsByGameOrdered(gameId);

        model.addAttribute("gameId", gameId);
        model.addAttribute("chapterNames", chapterNames);

        return "chapters_list";
    }

    // Страница 2 - Список Точек Решений внутри Главы
    // URL: /user/decisions/{gameId}/chapter/{chapterName}
    // Шаблон: decision_points_list.html
    // ==========================================================
    @GetMapping("/{gameId}/chapter/{chapterName}")
    public String listDecisionPointsInChapter(@PathVariable Long gameId, @PathVariable String chapterName, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String decodedChapterName = URLDecoder.decode(chapterName, StandardCharsets.UTF_8);

        User currentUser = getCurrentUser(userDetails);

        // ВСЕ точки решений для данной игры и Главы
        List<DecisionPoint> decisionPoints = decisionService.getDecisionPointsByGameAndChapterOrdered(gameId, decodedChapterName);

        // Решения пользователя для этих точек
        List<UserDecision> userDecisionsList = decisionService.getUserDecisionsForPoints(currentUser.getId(), decisionPoints);

        Set<Long> userMadeDecisionPointIds = userDecisionsList.stream()
                .map(UserDecision::getDecisionPoint)
                .map(DecisionPoint::getId)
                .collect(Collectors.toSet());

        model.addAttribute("gameId", gameId);
        model.addAttribute("chapterName", decodedChapterName);
        model.addAttribute("decisionPoints", decisionPoints);
        model.addAttribute("userMadeDecisionPointIds", userMadeDecisionPointIds);

        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }

        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }

        return "decision_points_list";
    }

    // Страница 3 - Выбор Решения
    // URL: /user/decisions/point/{pointId}
    // Шаблон: make_decision.html
    // ==========================================================
    @GetMapping("/point/{pointId}")
    public String makeDecision(@PathVariable Long pointId, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);

        DecisionPoint decisionPoint = decisionService.getDecisionPointById(pointId)
                .orElseThrow(() -> new RuntimeException("Decision Point not found with ID: " + pointId));

        Optional<UserDecision> userDecision = decisionService.getUserDecisionForPoint(currentUser.getId(), pointId);

        model.addAttribute("decisionPoint", decisionPoint);
        model.addAttribute("pointId", pointId);
        model.addAttribute("userDecision", userDecision.orElse(null));

        return "make_decision";
    }


    @PostMapping("/point/{pointId}/save")
    public String saveDecision(@PathVariable Long pointId,
                               @RequestParam("chosenChoiceId") Long chosenChoiceId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        try {
            decisionService.saveUserDecision(currentUser.getId(), pointId, chosenChoiceId);
            redirectAttributes.addFlashAttribute("successMessage", "Решение успешно сохранено!");

            DecisionPoint savedPoint = decisionService.getDecisionPointById(pointId)
                    .orElseThrow(() -> new RuntimeException("Decision Point not found after saving!"));

            Long gameId = savedPoint.getGame().getId();
            String chapterName = savedPoint.getChapterName();

            String encodedChapterName = URLEncoder.encode(chapterName, StandardCharsets.UTF_8);

            return "redirect:/user/decisions/" + gameId + "/chapter/" + encodedChapterName;


        } catch (Exception e) {
            logger.error("Error saving decision for point {}", pointId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при сохранении решения: " + e.getMessage());

            return "redirect:/user/decisions/point/" + pointId;
        }
    }
}