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

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepo commentRepo;
    private final RecipeRepo recipeRepo;
    private final UserRepo userRepo;
    private final LikeRepo likeRepo;

    public record CommentResponse(
            Integer id,
            String content,
            String author,
            String authorAvatar,
            Instant createdAt,
            long likes,
            long dislikes,
            ReactionType myReaction
    ) {}

    @Transactional
    public CommentResponse create(String email, Long recipeId, String content) {
        User user = userRepo.findByUsername(email).orElseThrow(() -> new RuntimeException("User not found"));
        Recipe recipe = recipeRepo.findById(recipeId.intValue())
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setRecipe(recipe);
        comment.setContent(content == null ? "" : content);

        commentRepo.save(comment);
        return map(comment, null);
    }

    public List<CommentResponse> listForRecipe(Long recipeId, String currentUserEmail) {
        Recipe recipe = recipeRepo.findById(recipeId.intValue())
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
        User currentUser = currentUserEmail == null ? null :
                userRepo.findByUsername(currentUserEmail).orElse(null);

        return commentRepo.findAllByRecipeOrderByCreatedAtDesc(recipe).stream()
                .map(c -> map(c, currentUser))
                .toList();
    }

    public CommentResponse get(Integer id, String currentUserEmail) {
        Comment comment = commentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        User currentUser = currentUserEmail == null ? null :
                userRepo.findByUsername(currentUserEmail).orElse(null);
        return map(comment, currentUser);
    }

    @Transactional
    public CommentResponse update(Integer id, String email, String newContent) {
        Comment comment = commentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        if (!comment.getUser().getUsername().equals(email)) {
            throw new RuntimeException("You can only edit your own comment");
        }
        comment.setContent(newContent == null ? "" : newContent);
        return map(comment, comment.getUser());
    }

    @Transactional
    public void delete(Integer id, String email) {
        Comment comment = commentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        if (!comment.getUser().getUsername().equals(email)) {
            throw new RuntimeException("You can only delete your own comment");
        }
        commentRepo.delete(comment);
    }

    private CommentResponse map(Comment comment, User currentUser) {
        long likes = likeRepo.countByCommentAndReactionType(comment, ReactionType.LIKE);
        long dislikes = likeRepo.countByCommentAndReactionType(comment, ReactionType.DISLIKE);
        ReactionType myReaction = null;
        if (currentUser != null) {
            myReaction = likeRepo.findByUserAndComment(currentUser, comment)
                    .map(Like::getReactionType)
                    .orElse(null);
        }

        String safeContent = comment.getContent() != null ? comment.getContent() : "";
        String author = comment.getUser() != null ? comment.getUser().getUsername() : "Anonim";
        String authorAvatar = comment.getUser() != null ? comment.getUser().getProfile_picture_url() : "";
        Instant createdAt = comment.getCreatedAt() != null ? comment.getCreatedAt().toInstant() : Instant.now();

        return new CommentResponse(
                comment.getCommentId(),
                safeContent,
                author,
                authorAvatar,
                createdAt,
                likes,
                dislikes,
                myReaction
        );
    }
}
