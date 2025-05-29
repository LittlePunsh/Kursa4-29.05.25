package org.kursplom.model;

import jakarta.persistence.*;
import java.util.Objects;

// Конкретный выбор для конкретной точки решения
@Entity
@Table(name = "configuration_choices", uniqueConstraints = @UniqueConstraint(columnNames = {"configuration_id", "decision_point_id"}))
public class ConfigurationChoice {
    @Id // Первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Связь ManyToOne с SaveConfiguration
    @JoinColumn(name = "configuration_id", nullable = false)
    private SaveConfiguration configuration; // Конфигурация мира, к которой относится выбор

    @ManyToOne(fetch = FetchType.LAZY) // Связь ManyToOne с DecisionPoint
    @JoinColumn(name = "decision_point_id", nullable = false)
    private DecisionPoint decisionPoint; // Точка решения

    @ManyToOne(fetch = FetchType.LAZY) // Связь ManyToOne с DecisionChoice
    @JoinColumn(name = "chosen_choice_id", nullable = false)
    private DecisionChoice chosenChoice; // Выбранный вариант для этой точки в этой конфигурации

    public ConfigurationChoice() {}

    // Конструктор для создания нового выбора конфигурации
    public ConfigurationChoice(SaveConfiguration configuration, DecisionPoint decisionPoint, DecisionChoice chosenChoice) {
        this.configuration = configuration;
        this.decisionPoint = decisionPoint;
        this.chosenChoice = chosenChoice;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SaveConfiguration getConfiguration() { return configuration; }
    public void setConfiguration(SaveConfiguration configuration) { this.configuration = configuration; }
    public DecisionPoint getDecisionPoint() { return decisionPoint; }
    public void setDecisionPoint(DecisionPoint decisionPoint) { this.decisionPoint = decisionPoint; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationChoice that = (ConfigurationChoice) o;
        // Используем ID для сравнения связанных сущностей
        return Objects.equals(configuration != null ? configuration.getId() : null, that.configuration != null ? that.configuration.getId() : null) &&
                Objects.equals(decisionPoint != null ? decisionPoint.getId() : null, that.decisionPoint != null ? that.decisionPoint.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuration != null ? configuration.getId() : null, decisionPoint != null ? decisionPoint.getId() : null);
    }

    @Override
    public String toString() {
        return "ConfigurationChoice{" +
                "id=" + id +
                ", configurationId=" + (configuration != null ? configuration.getId() : "null") +
                ", decisionPointId=" + (decisionPoint != null ? decisionPoint.getId() : "null") +
                ", chosenChoiceId=" + (chosenChoice != null ? chosenChoice.getId() : "null") +
                '}';
    }
}