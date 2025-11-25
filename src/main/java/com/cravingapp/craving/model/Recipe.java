package com.cravingapp.craving.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name="\"recipe\"")
public class Recipe {
    @Id
    @Column(name="recipe_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="user_id",nullable=false)
    private User user;
    @Column
    private String title;
    @Column
    private String description;
    @Column
    private Integer prep_time_minutes;
    @Column
    private Integer cook_time_minutes;
    @Column
    private Integer servings;
    @Column
    private LocalDateTime created_at;
    @OneToMany(mappedBy = "recipe_id", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> mediaList = new ArrayList<>();
    @OneToMany(mappedBy = "recipe_id",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<RecipeIngredient> recipeIngredients;

}




