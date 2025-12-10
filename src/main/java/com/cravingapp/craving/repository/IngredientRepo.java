package com.cravingapp.craving.repository;
import com.cravingapp.craving.model.Ingredient;
import com.cravingapp.craving.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepo extends JpaRepository<Ingredient, Integer> {
    Ingredient findIngredientById(Integer id);
    Optional<Ingredient> findIngredientByName(String name);
}
