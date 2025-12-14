package com.cravingapp.craving.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "\"like\"",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "recipe_id"}),
                @UniqueConstraint(columnNames = {"user_id", "comment_id"})
        }
)
@Data
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    private ReactionType reactionType;
}
