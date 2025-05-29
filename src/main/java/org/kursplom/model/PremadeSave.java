package org.kursplom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

// Сущность, представляющая готовый файл сохранения, который можно скачать
@Entity
@Table(name = "premade_saves")
public class PremadeSave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "configuration_id", nullable = false)
    private SaveConfiguration configuration; // Конфигурация решений

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game; // Игра, ДЛЯ КОТОРОЙ предназначен этот файл (т.е. какая игра его может импортировать, напр. W2)

    @Column(nullable = false)
    private String filename;

    @Column(columnDefinition = "TEXT")
    private String description; // Описание файла сохранения

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate; // Дата загрузки файла

    public PremadeSave() {
        this.uploadDate = LocalDateTime.now(); // Default upload date
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SaveConfiguration getConfiguration() { return configuration; }
    public void setConfiguration(SaveConfiguration configuration) { this.configuration = configuration; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PremadeSave that = (PremadeSave) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PremadeSave{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", configurationId=" + (configuration != null ? configuration.getId() : "null") +
                ", gameId=" + (game != null ? game.getId() : "null") +
                '}';
    }
}