package org.kursplom.model;

import jakarta.persistence.*;
import java.util.Set;
import java.util.Objects;

// Сущность, представляющая тип решения (глобальное, локальное)
@Entity
@Table(name = "decision_types")
public class DecisionType {
    @Id // Первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name; // Например, "GLOBAL", "LOCAL"

    private String description; // Описание типа решения

    @OneToMany(mappedBy = "decisionType", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DecisionPoint> decisionPoints;

    public DecisionType() {}

    public DecisionType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionType that = (DecisionType) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DecisionType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}