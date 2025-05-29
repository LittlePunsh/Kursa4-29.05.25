package org.kursplom.model;

import jakarta.persistence.*;
import java.util.Set;
import java.util.Objects;

// Сущность, представляющая один из вариантов выбора для точки решения
@Entity
@Table(name = "decision_choices", uniqueConstraints = @UniqueConstraint(columnNames = {"decision_point_id", "identifier"}))
public class DecisionChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Связь ManyToOne с DecisionPoint (lazy)
    @JoinColumn(name = "decision_point_id", nullable = false)
    private DecisionPoint decisionPoint; // Точка решения, к которой относится этот вариант

    @Column(nullable = false)
    private String identifier; // Уникальный строковый идентификатор выбора в рамках точки решения

    @Column(columnDefinition = "TEXT")
    private String description; // Описание самого варианта выбора

    @Column(name = "consequence_text", columnDefinition = "TEXT")
    private String consequenceText; // Описание последствий (опционально)

    @OneToMany(mappedBy = "chosenChoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserDecision> userDecisions;

    public DecisionChoice() {}

    // Конструктор для создания нового варианта выбора
    public DecisionChoice(DecisionPoint decisionPoint, String identifier, String description, String consequenceText) {
        this.decisionPoint = decisionPoint;
        this.identifier = identifier;
        this.description = description;
        this.consequenceText = consequenceText;
    }


    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DecisionPoint getDecisionPoint() { return decisionPoint; }
    public void setDecisionPoint(DecisionPoint decisionPoint) { this.decisionPoint = decisionPoint; }
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getConsequenceText() { return consequenceText; }
    public void setConsequenceText(String consequenceText) { this.consequenceText = consequenceText; }
    public Set<UserDecision> getUserDecisions() { return userDecisions; }
    public void setUserDecisions(Set<UserDecision> userDecisions) { this.userDecisions = userDecisions; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionChoice that = (DecisionChoice) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DecisionChoice{" +
                "id=" + id +
                ", identifier='" + identifier + '\'' +
                ", decisionPointId=" + (decisionPoint != null ? decisionPoint.getId() : "null") +
                '}';
    }
}