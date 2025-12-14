package com.cravingapp.craving.repository;

import com.cravingapp.craving.model.Comment;
import com.cravingapp.craving.model.Like;
import com.cravingapp.craving.model.ReactionType;
import com.cravingapp.craving.model.Recipe;
import com.cravingapp.craving.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepo extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndRecipe(User user, Recipe recipe);
    Optional<Like> findByUserAndComment(User user, Comment comment);
    long countByRecipeAndReactionType(Recipe recipe, ReactionType reactionType);
    long countByCommentAndReactionType(Comment comment, ReactionType reactionType);
}
