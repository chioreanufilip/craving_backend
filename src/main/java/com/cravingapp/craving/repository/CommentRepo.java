package com.cravingapp.craving.repository;

import com.cravingapp.craving.model.Comment;
import com.cravingapp.craving.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepo extends JpaRepository<Comment, Integer> {
    List<Comment> findAllByRecipeOrderByCreatedAtDesc(Recipe recipe);
}
