package com.cravingapp.craving.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="recipe_ingredient")
@Data
public class RecipeIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name="recipe_id")
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name="ingredient_id")
    private Ingredient ingredient;

    @Column
    private String quantity;
    @Column
    private String unit;
}
