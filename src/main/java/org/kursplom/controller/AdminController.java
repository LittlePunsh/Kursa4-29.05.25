package org.kursplom.controller;

import org.kursplom.model.*;
import org.kursplom.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService; // Для управления пользователями
    private final DecisionService decisionService; // Для управления контентом (точками/выборами) и решениями пользователей
    private final PremadeSaveService premadeSaveService; // Для управления готовыми сохранениями
    private final RoleService roleService; // Для получения списка ролей (для управления пользователями)
    private final GameService gameService; // Для получения списка игр (для контента/сохранений)
    private final DecisionTypeService decisionTypeService; // Для получения списка типов решений (для контента)
    private final SaveConfigurationService saveConfigurationService; // Для получения списка конфигураций (для сохранений)

    public AdminController(UserService userService,
                           DecisionService decisionService,
                           PremadeSaveService premadeSaveService,
                           RoleService roleService,
                           GameService gameService,
                           DecisionTypeService decisionTypeService,
                           SaveConfigurationService saveConfigurationService) {
        this.userService = userService;
        this.decisionService = decisionService;
        this.premadeSaveService = premadeSaveService;
        this.roleService = roleService;
        this.gameService = gameService;
        this.decisionTypeService = decisionTypeService;
        this.saveConfigurationService = saveConfigurationService;

        // Логирование для проверки внедрения сервисов
        if (this.userService == null) logger.error("AdminController: UserService was not injected!");
        if (this.decisionService == null) logger.error("AdminController: DecisionService was not injected!");
        if (this.premadeSaveService == null) logger.error("AdminController: PremadeSaveService was not injected!");
        if (this.roleService == null) logger.error("AdminController: RoleService was not injected!");
        if (this.gameService == null) logger.error("AdminController: GameService was not injected!");
        if (this.decisionTypeService == null) logger.error("AdminController: DecisionTypeService was not injected!");
        if (this.saveConfigurationService == null) logger.error("AdminController: SaveConfigurationService was not injected!");
    }

    // 1. Просмотр списков: Обзорная страница админки
    @GetMapping
    public String adminDashboard(Model model) {
        model.addAttribute("userCount", userService.getUserCount());
        model.addAttribute("gameCount", gameService.getGameCount());
        model.addAttribute("decisionPointCount", decisionService.getDecisionPointCount());
        model.addAttribute("premadeSaveCount", premadeSaveService.getPremadeSaveCount());
        model.addAttribute("saveConfigurationCount", saveConfigurationService.getSaveConfigurationCount());

        // Успех.com
        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }
        // Неудача.com
        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }


        return "admin/dashboard";
    }

    // 1. Просмотр списков: Пользователи
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);


        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }

        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }


        return "admin/users_list";
    }


    // 2. Управление пользователями: Просмотр и управление ролями
    @GetMapping("/users/{userId}")
    public String userDetail(@PathVariable Long userId, Model model) {
        logger.info("AdminController: Fetching details for user with ID: {}", userId);
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Role> allRoles = roleService.getAllRoles();
        List<Role> userRoles = user.getRoles().stream().toList();

        // Получаем решения пользователя
        List<UserDecision> userDecisions = decisionService.getAllUserDecisions(userId);
        logger.info("AdminController: Found {} decisions for user ID: {}", userDecisions.size(), userId);


        model.addAttribute("user", user);
        model.addAttribute("allRoles", allRoles);
        model.addAttribute("userRoles", userRoles);
        model.addAttribute("userDecisions", userDecisions);

        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }

        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }


        return "admin/user_detail";
    }

    // Обновление ролей пользователей
    @PostMapping("/users/{userId}/roles")
    public String updateUserRoles(@PathVariable Long userId,
                                  @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                                  RedirectAttributes redirectAttributes) {
        logger.info("AdminController: Attempting to update roles for user ID: {}", userId);
        try {
            userService.updateUserRoles(userId, roleIds);
            redirectAttributes.addFlashAttribute("successMessage", "Роли пользователя успешно обновлены!");
            logger.info("AdminController: Roles updated successfully for user ID: {}", userId);
        } catch (Exception e) {
            logger.error("AdminController: Error updating roles for user {}", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении ролей: " + e.getMessage());
        }
        return "redirect:/admin/users/" + userId;
    }

    // Удаление пользователя
    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        logger.info("AdminController: Attempting to delete user with ID: {}", userId);
        try {
            boolean deleted = userService.deleteUser(userId);
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно удален.");
                logger.info("AdminController: User with ID {} deleted successfully.", userId);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Пользователь с ID " + userId + " не найден.");
                logger.warn("AdminController: User with ID {} not found for deletion.", userId);
            }
        } catch (Exception e) {
            logger.error("AdminController: Error deleting user {}", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении пользователя: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // 1. Просмотр списков: Игры
    @GetMapping("/games")
    public String listGames(Model model) {
        List<Game> games = gameService.getAllGames();
        model.addAttribute("games", games);


        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }

        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }


        return "admin/games_list";
    }

    // 1. Просмотр списков: Главы/Квесты/Точки решений для конкретной игры
    // Просмотр Глав игры
    @GetMapping("/games/{gameId}/content")
    public String listGameContentChapters(@PathVariable Long gameId, Model model) {
        Game game = gameService.getGameById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        List<String> chapterNames = decisionService.getChaptersWithDecisionsByGameOrdered(gameId);

        model.addAttribute("game", game);
        model.addAttribute("chapterNames", chapterNames);

        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }

        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }


        return "admin/game_content_chapters";
    }

    // Просмотр Квестов в конкретной Главе игры
    @GetMapping("/games/{gameId}/content/chapter/{chapterName}")
    public String listGameChapterQuests(@PathVariable Long gameId, @PathVariable String chapterName, Model model) {
        Game game = gameService.getGameById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        String decodedChapterName = URLDecoder.decode(chapterName, StandardCharsets.UTF_8);

        List<String> questNames = decisionService.getQuestsWithDecisionsByGameAndChapterOrdered(gameId, decodedChapterName);

        model.addAttribute("game", game);
        model.addAttribute("chapterName", decodedChapterName);
        model.addAttribute("questNames", questNames);


        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }

        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }


        return "admin/game_content_quests";
    }

    // Просмотр Точек Решений в конкретном Квесте главы
    @GetMapping("/games/{gameId}/content/chapter/{chapterName}/quest/{questName}")
    public String listGameQuestPoints(@PathVariable Long gameId, @PathVariable String chapterName, @PathVariable String questName, Model model) {
        Game game = gameService.getGameById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        String decodedChapterName = URLDecoder.decode(chapterName, StandardCharsets.UTF_8);
        String decodedQuestName = URLDecoder.decode(questName, StandardCharsets.UTF_8);

        List<DecisionPoint> decisionPoints = decisionService.getDecisionPointsByGameAndChapterAndQuest(gameId, decodedChapterName, decodedQuestName);

        model.addAttribute("game", game);
        model.addAttribute("chapterName", decodedChapterName);
        model.addAttribute("questName", decodedQuestName);
        model.addAttribute("decisionPoints", decisionPoints);


        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }

        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }


        return "admin/game_content_points";
    }

    // 3. Управление контентом: Просмотр, Редактирование, Удаление Точек Решений
    // Просмотр деталей конкретной Точки Решения (и ее вариантов)
    @GetMapping("/points/{pointId}/detail")
    public String viewDecisionPointDetail(@PathVariable Long pointId, Model model) {
        logger.info("AdminController: Fetching details for DecisionPoint with ID: {}", pointId);
        DecisionPoint decisionPoint = decisionService.getDecisionPointById(pointId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Decision Point not found"));

        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }

        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }


        model.addAttribute("point", decisionPoint);
        model.addAttribute("game", decisionPoint.getGame());
        model.addAttribute("decisionType", decisionPoint.getDecisionType());

        return "admin/decision_point_detail";
    }

    // Редактирование точек решения
    @GetMapping("/points/{pointId}/edit")
    public String editDecisionPointForm(@PathVariable Long pointId, Model model) {
        logger.info("AdminController: Request to edit DecisionPoint with ID: {}", pointId);
        DecisionPoint decisionPoint = decisionService.getDecisionPointById(pointId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Decision Point not found"));

        List<Game> games = gameService.getAllGames();
        List<DecisionType> decisionTypes = decisionTypeService.getAllDecisionTypes();

        model.addAttribute("decisionPoint", decisionPoint);
        model.addAttribute("games", games);
        model.addAttribute("decisionTypes", decisionTypes);

        return "admin/edit_decision_point";
    }

    // Редактирование Точки решения
    @PostMapping("/points/{pointId}/edit")
    public String updateDecisionPoint(@PathVariable Long pointId,
                                      @RequestParam Long gameId,
                                      @RequestParam Long decisionTypeId,
                                      @RequestParam String chapterName,
                                      @RequestParam String questName,
                                      @RequestParam String identifier,
                                      @RequestParam String description,
                                      @RequestParam(required = false) Integer orderInGame,
                                      RedirectAttributes redirectAttributes) {
        logger.info("AdminController: Attempting to update DecisionPoint with ID: {}", pointId);
        try {
            // Временный объект, чтобы передать данные в сервис
            DecisionPoint updatedPointData = new DecisionPoint();
            updatedPointData.setChapterName(chapterName);
            updatedPointData.setQuestName(questName);
            updatedPointData.setIdentifier(identifier);
            updatedPointData.setDescription(description);
            updatedPointData.setOrderInGame(orderInGame);


            Optional<DecisionPoint> updatedPoint = decisionService.updateDecisionPoint(pointId, updatedPointData, gameId, decisionTypeId);

            if (updatedPoint.isPresent()) {
                redirectAttributes.addFlashAttribute("successMessage", "Точка решения успешно обновлена!");
                logger.info("AdminController: DecisionPoint with ID {} updated successfully.", pointId);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении точки решения: Точка с ID " + pointId + " не найдена.");
                logger.warn("AdminController: Update failed, DecisionPoint with ID {} not found.", pointId);
            }

        } catch (IllegalArgumentException e) {
            logger.error("AdminController: Error updating decision point {}: Invalid data.", pointId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении точки решения: " + e.getMessage());
            return "redirect:/admin/points/" + pointId + "/edit";
        }
        catch (Exception e) {
            logger.error("AdminController: Неизвестная ошибка при обновлении точки решения {}", pointId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Неизвестная ошибка при обновлении точки решения: " + e.getMessage());
        }
        return "redirect:/admin/points/" + pointId + "/detail";
    }


    // Удаление Точки решения
    @PostMapping("/points/{pointId}/delete")
    public String deleteDecisionPoint(@PathVariable Long pointId, RedirectAttributes redirectAttributes) {
        logger.info("AdminController: Attempting to delete DecisionPoint with ID: {}", pointId);
        try {
            Optional<DecisionPoint> pointToDeleteOptional = decisionService.getDecisionPointById(pointId);
            if (pointToDeleteOptional.isEmpty()) {
                logger.warn("AdminController: DecisionPoint with ID {} not found for deletion.", pointId);
                redirectAttributes.addFlashAttribute("errorMessage", "Точка решения с ID " + pointId + " не найдена для удаления.");
                return "redirect:/admin/games";
            }
            DecisionPoint pointToDelete = pointToDeleteOptional.get();
            Long gameId = pointToDelete.getGame().getId();
            String chapterName = pointToDelete.getChapterName();
            String questName = pointToDelete.getQuestName();

            boolean deleted = decisionService.deleteDecisionPoint(pointId);

            if (deleted) {
                logger.info("AdminController: DecisionPoint with ID {} deleted successfully.", pointId);
                redirectAttributes.addFlashAttribute("successMessage", "Точка решения успешно удалена.");
                String encodedChapterName = URLEncoder.encode(chapterName, StandardCharsets.UTF_8);
                String encodedQuestName = URLEncoder.encode(questName, StandardCharsets.UTF_8);
                return "redirect:/admin/games/" + gameId + "/content/chapter/" + encodedChapterName + "/quest/" + encodedQuestName;

            } else {
                logger.warn("AdminController: DecisionService reported deletion failed for ID {}.", pointId);
                redirectAttributes.addFlashAttribute("errorMessage", "Точка решения с ID " + pointId + " не найдена или удаление не удалось.");
                String encodedChapterName = URLEncoder.encode(chapterName, StandardCharsets.UTF_8);
                String encodedQuestName = URLEncoder.encode(questName, StandardCharsets.UTF_8);
                return "redirect:/admin/games/" + gameId + "/content/chapter/" + encodedChapterName + "/quest/" + encodedQuestName;
            }
        } catch (Exception e) {
            logger.error("AdminController: Ошибка при удалении точки решения {}", pointId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении точки решения: " + e.getMessage());
            return "redirect:/admin";
        }
    }


    // 3. БУправление контентом: Просмотр, Редактирование, Удаление Вариантов Выбора
    // Редактирование Варианта выбора
    @GetMapping("/choices/{choiceId}/edit")
    public String editDecisionChoiceForm(@PathVariable Long choiceId, Model model) {
        logger.info("AdminController: Request to edit DecisionChoice with ID: {}", choiceId);
        DecisionChoice decisionChoice = decisionService.getDecisionChoiceById(choiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Decision Choice not found"));

        model.addAttribute("decisionChoice", decisionChoice);
        model.addAttribute("point", decisionChoice.getDecisionPoint());


        return "admin/edit_decision_choice";
    }

    // Редактирования Варианта Выбора
    @PostMapping("/choices/{choiceId}/edit")
    public String updateDecisionChoice(@PathVariable Long choiceId,
                                       @RequestParam String identifier,
                                       @RequestParam String description,
                                       @RequestParam(required = false) String consequenceText,
                                       RedirectAttributes redirectAttributes) {
        logger.info("AdminController: Attempting to update DecisionChoice with ID: {}", choiceId);
        try {
            // Временный объект для передачи данных в сервис
            DecisionChoice updatedChoiceData = new DecisionChoice();
            updatedChoiceData.setIdentifier(identifier);
            updatedChoiceData.setDescription(description);
            updatedChoiceData.setConsequenceText(consequenceText);


            Optional<DecisionChoice> updatedChoice = decisionService.updateDecisionChoice(choiceId, updatedChoiceData);

            if (updatedChoice.isPresent()) {
                redirectAttributes.addFlashAttribute("successMessage", "Вариант выбора успешно обновлен!");
                logger.info("AdminController: DecisionChoice with ID {} updated successfully.", choiceId);
                return "redirect:/admin/points/" + updatedChoice.get().getDecisionPoint().getId() + "/detail";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении варианта выбора: Вариант с ID " + choiceId + " не найден.");
                logger.warn("AdminController: Update failed, DecisionChoice with ID {} not found.", choiceId);
                return "redirect:/admin/choices/" + choiceId + "/edit";
            }

        } catch (Exception e) {
            logger.error("AdminController: Ошибка при обновлении варианта выбора {}", choiceId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении варианта выбора: " + e.getMessage());
            return "redirect:/admin/choices/" + choiceId + "/edit";
        }
    }


    // Удаление варианта выбора
    @PostMapping("/choices/{choiceId}/delete")
    public String deleteDecisionChoice(@PathVariable Long choiceId, RedirectAttributes redirectAttributes) {
        logger.info("AdminController: Attempting to delete DecisionChoice with ID: {}", choiceId);
        try {
            Optional<DecisionChoice> choiceToDeleteOptional = decisionService.getDecisionChoiceById(choiceId);
            if (choiceToDeleteOptional.isEmpty()) {
                logger.warn("AdminController: DecisionChoice with ID {} not found for deletion.", choiceId);
                redirectAttributes.addFlashAttribute("errorMessage", "Вариант выбора с ID " + choiceId + " не найден для удаления.");
                return "redirect:/admin/games"; // Fallback
            }
            DecisionChoice choiceToDelete = choiceToDeleteOptional.get();
            Long pointId = choiceToDelete.getDecisionPoint().getId();


            boolean deleted = decisionService.deleteDecisionChoice(choiceId);

            if (deleted) {
                logger.info("AdminController: DecisionChoice with ID {} deleted successfully.", choiceId);
                redirectAttributes.addFlashAttribute("successMessage", "Вариант выбора успешно удален.");
                return "redirect:/admin/points/" + pointId + "/detail";

            } else {
                logger.warn("AdminController: DecisionService reported deletion failed for ID {}.", choiceId);
                redirectAttributes.addFlashAttribute("errorMessage", "Вариант выбора с ID " + choiceId + " не найден или удаление не удалось.");
                return "redirect:/admin/points/" + pointId + "/detail";
            }
        } catch (Exception e) {
            logger.error("AdminController: Ошибка при удалении варианта выбора {}", choiceId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении варианта выбора: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    // 1. Просмотр списков: Готовые сохранения
    @GetMapping("/saves")
    public String listPremadeSaves(Model model) {
        List<PremadeSave> saves = premadeSaveService.getAllPremadeSaves();
        model.addAttribute("saves", saves);


        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));

        }
        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }


        return "admin/premade_saves_list";
    }

    // 4. Управление готовыми сохранениями: Загрузка и связывание
    @GetMapping("/saves/upload")
    public String uploadPremadeSaveForm(Model model) {
        List<SaveConfiguration> configurations = saveConfigurationService.getAllSaveConfigurations();
        List<Game> games = gameService.getAllGames();

        model.addAttribute("configurations", configurations);
        model.addAttribute("games", games);


        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }

        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }


        return "admin/upload_premade_save";
    }

    // Загрузки файла и связывания с конфигурацией
    @PostMapping("/saves/upload")
    public String handlePremadeSaveUpload(@RequestParam("file") MultipartFile file,
                                          @RequestParam("configurationId") Long configurationId,
                                          @RequestParam("targetGameId") Long targetGameId,
                                          @RequestParam("description") String description,
                                          RedirectAttributes redirectAttributes) {
        logger.info("AdminController: Attempting to upload and link premade save.");
        try {
            SaveConfiguration configuration = saveConfigurationService.getSaveConfigurationById(configurationId)
                    .orElseThrow(() -> new IllegalArgumentException("Save configuration not found with ID: " + configurationId));
            Game targetGame = gameService.getGameById(targetGameId)
                    .orElseThrow(() -> new IllegalArgumentException("Target game not found with ID: " + targetGameId));

            premadeSaveService.uploadAndLinkSave(file, configuration, targetGame, description);

            redirectAttributes.addFlashAttribute("successMessage", "Файл сохранения успешно загружен и связан!");
            logger.info("AdminController: Premade save uploaded and linked successfully.");

        } catch (IllegalArgumentException e) {
            logger.error("AdminController: Error uploading and linking premade save: Invalid data.", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при загрузке файла сохранения: " + e.getMessage());
            return "redirect:/admin/saves/upload";
        } catch (Exception e) {
            logger.error("AdminController: Неизвестная ошибка при загрузке файла сохранения", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Неизвестная ошибка при загрузке файла сохранения: " + e.getMessage());
            return "redirect:/admin/saves/upload";
        }

        return "redirect:/admin/saves";
    }

    // 1. Просмотр списков: Конфигурации сохранений
    @GetMapping("/configs")
    public String listSaveConfigurations(Model model) {
        List<SaveConfiguration> configurations = saveConfigurationService.getAllSaveConfigurations();
        model.addAttribute("configurations", configurations);

        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }

        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }

        return "admin/save_configurations_list";
    }

    // Просмотр деталей конфигурации сохранений
    @GetMapping("/configs/{configId}")
    public String viewSaveConfigurationDetail(@PathVariable Long configId, Model model) {
        logger.info("AdminController: Fetching details for SaveConfiguration with ID: {}", configId);
        SaveConfiguration configuration = saveConfigurationService.getSaveConfigurationById(configId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Save Configuration not found"));

        model.addAttribute("configuration", configuration);
        model.addAttribute("originatingGame", configuration.getOriginatingGame()); // Добавляем исходную игру

        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }

        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }

        return "admin/save_configuration_detail";
    }

}