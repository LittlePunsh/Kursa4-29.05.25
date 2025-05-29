package org.kursplom.controller;

import org.kursplom.model.Game;
import org.kursplom.service.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PageController {

    private final GameService gameService;

    public PageController(GameService gameService) {
        this.gameService = gameService;
    }

    // Отображаем страницу входа
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Отображаем страницу регистрации
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    // Отображает страницу пользователя
    @GetMapping("/user")
    public String userHomePage(Model model) {
        // Получаем список всех игр
        List<Game> games = gameService.getAllGames();
        model.addAttribute("games", games);

        return "user/game_selection";
    }

    // Домашняя страница, перенаправляет на страницу пользователя или входа
    @GetMapping("/")
    public String homePage() {

        return "redirect:/user";
    }
}