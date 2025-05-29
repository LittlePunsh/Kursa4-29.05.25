package org.kursplom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class WitcherKeepApplication {

    private static final Logger logger = LoggerFactory.getLogger(WitcherKeepApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WitcherKeepApplication.class, args);

        logger.info("WitcherKeepApplication has started successfully!");
    }
}
//Обычный пользователь Логин - LittlePunsh, Пароль - LittlePunsh
//Админ Логин - admin, Пароль - admin123

//DELETE FROM user_decisions;
//DELETE FROM configuration_choices;
//DELETE FROM premade_saves;
//DELETE FROM decision_choices;
//DELETE FROM decision_points;
//DELETE FROM save_configurations;
