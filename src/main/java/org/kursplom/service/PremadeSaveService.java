package org.kursplom.service;

import org.kursplom.model.PremadeSave;
import org.kursplom.model.SaveConfiguration;
import org.kursplom.model.Game;
import org.kursplom.repository.PremadeSaveRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;



@Service
public class PremadeSaveService {

    private static final Logger logger = LoggerFactory.getLogger(PremadeSaveService.class);

    private final PremadeSaveRepository premadeSaveRepository;

    @Value("${app.premade-saves.base-dir}")
    private String baseSaveDirectory;

    public PremadeSaveService(PremadeSaveRepository premadeSaveRepository) {
        this.premadeSaveRepository = premadeSaveRepository;
    }

    public List<PremadeSave> getAllPremadeSaves() {
        return premadeSaveRepository.findAll();
    }

    public long getPremadeSaveCount() {
        return premadeSaveRepository.count();
    }

    // Метод для загрузки файла и связывания его с PremadeSave
    @Transactional
    public PremadeSave uploadAndLinkSave(MultipartFile file, SaveConfiguration configuration, Game targetGame, String description) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        // Определяем путь для сохранения файла
        Path uploadPath = Paths.get(baseSaveDirectory, file.getOriginalFilename()); // Используем оригинальное имя файла

        // Создаем директорию, если она не существует
        Files.createDirectories(uploadPath.getParent());

        // Сохраняем файл
        Files.copy(file.getInputStream(), uploadPath);
        logger.info("File saved to {}", uploadPath.toAbsolutePath());

        // Создаем новую запись в БД PremadeSave
        PremadeSave premadeSave = new PremadeSave();
        premadeSave.setConfiguration(configuration);
        premadeSave.setGame(targetGame);
        premadeSave.setFilename(file.getOriginalFilename()); // Храним только имя файла
        premadeSave.setDescription(description);

        // Сохраняем запись в БД
        PremadeSave savedPremadeSave = premadeSaveRepository.save(premadeSave);
        logger.info("PremadeSave record saved with ID {}", savedPremadeSave.getId());

        return savedPremadeSave;
    }

}