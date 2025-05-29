package org.kursplom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

// Сущность, представляющая решение, принятое конкретным пользователем для конкретной точки решения
@Entity
@Table(name = "user_decisions", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "decision_point_id"}))
public class UserDecision {
    @Id // Первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Пользователь, принявший решение

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_point_id", nullable = false)
    private DecisionPoint decisionPoint; // Точка решения

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chosen_choice_id", nullable = false)
    private DecisionChoice chosenChoice; // Выбранный вариант

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // Дата и время фиксации решения

    public UserDecision() {
        this.createdAt = LocalDateTime.now();
    }

    // Конструктор для создания нового решения пользователя
    public UserDecision(User user, DecisionPoint decisionPoint, DecisionChoice chosenChoice) {
        this.user = user;
        this.decisionPoint = decisionPoint;
        this.chosenChoice = chosenChoice;
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public DecisionPoint getDecisionPoint() { return decisionPoint; }
    public void setDecisionPoint(DecisionPoint decisionPoint) { this.decisionPoint = decisionPoint; }
    public DecisionChoice getChosenChoice() { return chosenChoice; }
    public void setChosenChoice(DecisionChoice chosenChoice) { this.chosenChoice = chosenChoice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDecision that = (UserDecision) o;
        return Objects.equals(user != null ? user.getId() : null, that.user != null ? that.user.getId() : null) &&
                Objects.equals(decisionPoint != null ? decisionPoint.getId() : null, that.decisionPoint != null ? that.decisionPoint.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user != null ? user.getId() : null, decisionPoint != null ? decisionPoint.getId() : null);
    }

    @Override
    public String toString() {
        return "UserDecision{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", decisionPointId=" + (decisionPoint != null ? decisionPoint.getId() : "null") +
                ", chosenChoiceId=" + (chosenChoice != null ? chosenChoice.getId() : "null") +
                '}';
    }
}