package com.cravingapp.craving.controller;

import com.cravingapp.craving.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    public record CreateCommentRequest(Long recipeId, String content) {}
    public record UpdateCommentRequest(String content) {}

    @PostMapping
    public ResponseEntity<CommentService.CommentResponse> create(Authentication authentication,
                                                                 @RequestBody CreateCommentRequest request) {
        var response = commentService.create(authentication.getName(), request.recipeId(), request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentService.CommentResponse> get(@PathVariable Integer id,
                                                              Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(commentService.get(id, email));
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<List<CommentService.CommentResponse>> listByRecipe(@PathVariable Long recipeId,
                                                                             Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(commentService.listForRecipe(recipeId, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentService.CommentResponse> update(@PathVariable Integer id,
                                                                 @RequestBody UpdateCommentRequest request,
                                                                 Authentication authentication) {
        var response = commentService.update(id, authentication.getName(), request.content());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, Authentication authentication) {
        commentService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
