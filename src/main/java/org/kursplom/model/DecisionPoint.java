package org.kursplom.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "decision_points", uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "identifier"}))
public class DecisionPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "decision_type_id", nullable = false)
    private DecisionType decisionType;

    @Column(nullable = false)
    private String identifier;

    @Column(name = "chapter_name")
    private String chapterName;

    @Column(name = "quest_name")
    private String questName;

    @Column(columnDefinition = "TEXT")
    private String description;


    @Column(name = "order_in_game")
    private Integer orderInGame;

    @OneToMany(mappedBy = "decisionPoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DecisionChoice> choices;

    @OneToMany(mappedBy = "decisionPoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserDecision> userDecisions;

    public DecisionPoint() {}

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public DecisionType getDecisionType() { return decisionType; }
    public void setDecisionType(DecisionType decisionType) { this.decisionType = decisionType; }
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getChapterName() { return chapterName; }
    public void setChapterName(String chapterName) { this.chapterName = chapterName; }

    public String getQuestName() { return questName; }
    public void setQuestName(String questName) { this.questName = questName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getOrderInGame() { return orderInGame; }
    public void setOrderInGame(Integer orderInGame) { this.orderInGame = orderInGame; }
    public Set<DecisionChoice> getChoices() { return choices; }
    public void setChoices(Set<DecisionChoice> choices) { this.choices = choices; }

}