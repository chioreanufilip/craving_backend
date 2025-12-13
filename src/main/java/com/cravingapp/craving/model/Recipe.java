package com.cravingapp.craving.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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
    @Column(name = "prep_time_minutes")
    private Integer prepTimeMinutes;
    @Column(name = "cook_time_minutes")
    private Integer cookTimeMinutes;
    @Column
    private Integer servings;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> mediaList = new ArrayList<>();
    @OneToMany(mappedBy = "recipe",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<RecipeIngredient> recipeIngredients = new ArrayList<>();
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "recipe",cascade =  CascadeType.ALL,orphanRemoval = true)
    private List<Step> steps = new ArrayList<>();
}



