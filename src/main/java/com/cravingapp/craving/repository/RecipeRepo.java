package com.cravingapp.craving.repository;


import com.cravingapp.craving.model.Recipe;
import com.cravingapp.craving.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepo extends JpaRepository<Recipe, Integer> {
    List<Recipe> getRecipeByUser(User user);
    @Query("SELECT r " +
            "FROM Recipe r " +
            "JOIN r.recipeIngredients ri " +
            "JOIN ri.ingredient i " +
            "WHERE LOWER(i.name) IN :ingredients " + // 1. Filtrează doar cele care au măcar un ingredient
            "GROUP BY r.id " +                       // 2. Grupează ca să putem număra
            "ORDER BY COUNT(i.id) DESC")             // 3. Pune-le primele pe cele cu cele mai multe potriviri
    List<Recipe> findBestMatchingRecipes(@Param("ingredients") List<String> ingredients);

    List<Recipe> findAllByOrderByCreatedAtDesc();
    List<Recipe> findAllByUserOrderByCreatedAtDesc(User user);
}
