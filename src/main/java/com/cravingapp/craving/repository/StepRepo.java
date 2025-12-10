package com.cravingapp.craving.repository;
import com.cravingapp.craving.model.Step;
import com.cravingapp.craving.model.Recipe;
import com.cravingapp.craving.model.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StepRepo extends JpaRepository<Step, Integer> {
    List<Step> getStepByRecipe(Recipe recipe);
}

