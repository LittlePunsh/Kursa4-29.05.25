package org.kursplom.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

// Сущность, описывающая предопределенный набор глобальных решений
@Entity
@Table(name = "save_configurations")
public class SaveConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // Название конфигурации

    @Column(columnDefinition = "TEXT")
    private String description; // Описание этой конфигурации мира

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "originating_game_id", nullable = false)
    private Game originatingGame; // Игра, решения которой описывает эта конфигурация (напр. W1)

    @OneToMany(mappedBy = "configuration", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ConfigurationChoice> configurationChoices = new HashSet<>(); // Набор выборов в этой конфигурации

    @OneToMany(mappedBy = "configuration", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PremadeSave> premadeSaves;

    public SaveConfiguration() {}

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Game getOriginatingGame() { return originatingGame; }
    public void setOriginatingGame(Game originatingGame) { this.originatingGame = originatingGame; }

    // Вспомогательный метод для добавления выбора к конфигурации
    public void addChoice(DecisionPoint point, DecisionChoice choice) {
        ConfigurationChoice configChoice = new ConfigurationChoice(this, point, choice);
        if (this.configurationChoices == null) {
            this.configurationChoices = new HashSet<>();
        }
        this.configurationChoices.add(configChoice);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaveConfiguration that = (SaveConfiguration) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SaveConfiguration{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", originatingGameId=" + (originatingGame != null ? originatingGame.getId() : "null") +
                '}';
    }
}