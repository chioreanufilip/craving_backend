package com.cravingapp.craving.repository;

import com.cravingapp.craving.model.Media;
import com.cravingapp.craving.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface MediaRepo extends JpaRepository<Media, Integer> {
    List<Media> getMediaByRecipe(Recipe recipe);
}
