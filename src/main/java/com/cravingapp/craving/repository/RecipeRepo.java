package com.cravingapp.craving.repository;


import com.cravingapp.craving.model.Recipe;
import com.cravingapp.craving.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepo extends JpaRepository<Recipe, Integer> {
    List<Recipe> getRecipeByUser(User user);
}
