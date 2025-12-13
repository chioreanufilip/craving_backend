package com.cravingapp.craving.service;

import com.cravingapp.craving.model.Comment;
import com.cravingapp.craving.model.Like;
import com.cravingapp.craving.model.ReactionType;
import com.cravingapp.craving.model.Recipe;
import com.cravingapp.craving.model.User;
import com.cravingapp.craving.repository.CommentRepo;
import com.cravingapp.craving.repository.LikeRepo;
import com.cravingapp.craving.repository.RecipeRepo;
import com.cravingapp.craving.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionService {
    private final LikeRepo likeRepo;
    private final UserRepo userRepo;
    private final RecipeRepo recipeRepo;
    private final CommentRepo commentRepo;

    public record ReactionResponse(long likes, long dislikes, ReactionType myReaction) {}

    @Transactional
    public ReactionResponse reactToRecipe(String email, Long recipeId, ReactionType reactionType) {
        User user = userRepo.findByUsername(email).orElseThrow(() -> new RuntimeException("User not found"));
        Recipe recipe = recipeRepo.findById(recipeId.intValue())
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        Like existing = likeRepo.findByUserAndRecipe(user, recipe).orElse(null);
        if (existing != null) {
            if (existing.getReactionType() == reactionType) {
                likeRepo.delete(existing); // toggle off
            } else {
                existing.setReactionType(reactionType);
            }
        } else {
            Like like = new Like();
            like.setUser(user);
            like.setRecipe(recipe);
            like.setReactionType(reactionType);
            likeRepo.save(like);
        }

        long likes = likeRepo.countByRecipeAndReactionType(recipe, ReactionType.LIKE);
        long dislikes = likeRepo.countByRecipeAndReactionType(recipe, ReactionType.DISLIKE);
        ReactionType myReaction = likeRepo.findByUserAndRecipe(user, recipe)
                .map(Like::getReactionType)
                .orElse(null);
        return new ReactionResponse(likes, dislikes, myReaction);
    }

    @Transactional
    public ReactionResponse reactToComment(String email, Integer commentId, ReactionType reactionType) {
        User user = userRepo.findByUsername(email).orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Like existing = likeRepo.findByUserAndComment(user, comment).orElse(null);
        if (existing != null) {
            if (existing.getReactionType() == reactionType) {
                likeRepo.delete(existing);
            } else {
                existing.setReactionType(reactionType);
            }
        } else {
            Like like = new Like();
            like.setUser(user);
            like.setComment(comment);
            like.setReactionType(reactionType);
            likeRepo.save(like);
        }

        long likes = likeRepo.countByCommentAndReactionType(comment, ReactionType.LIKE);
        long dislikes = likeRepo.countByCommentAndReactionType(comment, ReactionType.DISLIKE);
        ReactionType myReaction = likeRepo.findByUserAndComment(user, comment)
                .map(Like::getReactionType)
                .orElse(null);
        return new ReactionResponse(likes, dislikes, myReaction);
    }
}
