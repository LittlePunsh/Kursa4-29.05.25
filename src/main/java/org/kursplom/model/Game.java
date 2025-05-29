package org.kursplom.model;

import jakarta.persistence.*;
import java.util.Set;
import java.util.Objects;

// Сущность, представляющая игру серии Ведьмак (W1, W2, W3)
@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Автоинкремент
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // Например, "The Witcher", "The Witcher 2", "The Witcher 3"

    @Column(name = "release_year")
    private Integer releaseYear; // Год выпуска

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DecisionPoint> decisionPoints; // Сет точек решений в этой игре

    public Game() {}

    // Конструктор для создания новой игры
    public Game(String name, Integer releaseYear) {
        this.name = name;
        this.releaseYear = releaseYear;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public Set<DecisionPoint> getDecisionPoints() { return decisionPoints; }
    public void setDecisionPoints(Set<DecisionPoint> decisionPoints) { this.decisionPoints = decisionPoints; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(id, game.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}