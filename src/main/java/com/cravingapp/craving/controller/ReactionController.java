package com.cravingapp.craving.controller;

import com.cravingapp.craving.model.ReactionType;
import com.cravingapp.craving.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {
    private final ReactionService reactionService;

    public record ReactionRequest(ReactionType reactionType) {}

    @PostMapping("/recipes/{recipeId}")
    public ResponseEntity<ReactionService.ReactionResponse> reactToRecipe(
            @PathVariable Long recipeId,
            @RequestBody ReactionRequest request,
            Authentication authentication
    ) {
        var response = reactionService.reactToRecipe(authentication.getName(), recipeId, request.reactionType());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/comments/{commentId}")
    public ResponseEntity<ReactionService.ReactionResponse> reactToComment(
            @PathVariable Integer commentId,
            @RequestBody ReactionRequest request,
            Authentication authentication
    ) {
        var response = reactionService.reactToComment(authentication.getName(), commentId, request.reactionType());
        return ResponseEntity.ok(response);
    }
}
