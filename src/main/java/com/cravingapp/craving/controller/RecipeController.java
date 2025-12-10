package com.cravingapp.craving.controller;


import com.cravingapp.craving.model.Recipe;
import com.cravingapp.craving.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.cravingapp.craving.service.RecipeService.RecipeResponse;

import java.util.List;

@RestController
@RequestMapping("api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    public record RecipeCreateRequest(String title, String description, Integer prep_time_minutes,
                                      Integer cook_time_minutes, Integer servings,
                                      List<RecipeService.IngredientDTO> ingredients, List<String> steps) {
    }
    public record RecipeUpdateRequest(
            String title,
            String description,
            Integer prepTimeMinutes,
            Integer cookTimeMinutes,
            Integer servings,
            List<RecipeService.IngredientDTO> ingredients,
            List<String> steps,
            List<Long> mediaIdsToDelete
    ) {}

    public record FeedPostResponse(
            Integer id,
            String title,
            String username,       // Numele celui care a postat
            String userAvatarUrl,  // Poza celui care a postat
            String imageUrl,       // Prima poză a rețetei (Cover)
            Integer likes,         // Numărul de like-uri
            Integer comments,      // Numărul de comentarii
            Boolean isLiked,
            String source          // "LOCAL" (Important pentru click!)
    ) {
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createRecipe(Authentication authentication,
                                          @RequestPart("data") RecipeCreateRequest recipeCreateRequest,
                                          @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        RecipeResponse recipe = recipeService.createRecipe(authentication, recipeCreateRequest, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(recipe);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RecipeResponse>> searchRecipes(
            @RequestParam List<String> ingredients
    ) {
        // Apelăm metoda hibridă (Local DB + Spoonacular)
        List<RecipeResponse> results = recipeService.hybridSearch(ingredients);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getLocalRecipe(@PathVariable Long id) {
        RecipeResponse response = recipeService.getLocalRecipe(id);
        return ResponseEntity.ok(response);
    }

    // --- 3. DETALII SPOONACULAR (GET) ---
    // Folosit când dai click pe o rețetă externă
    @GetMapping("/spoonacular/{id}")
    public ResponseEntity<RecipeResponse> getSpoonacularDetails(@PathVariable Long id) {
        // Presupunând că ai metoda asta în Service (discutată anterior)
        // return ResponseEntity.ok(recipeService.getSpoonacularDetails(id));
        RecipeResponse response = recipeService.getSpoonacularRecipe(id);
        return ResponseEntity.ok(response); // Placeholder până implementezi metoda
    }

    @GetMapping("/feed")
    public ResponseEntity<List<FeedPostResponse>> getFeed(Authentication authentication) {
        String email = authentication.getName();

        // Returnează direct lista pe care o vrea Android-ul
        return ResponseEntity.ok(recipeService.getAllFeedPosts(email));
    }
    @GetMapping("/my-posts")
    public ResponseEntity<List<FeedPostResponse>> getMyPosts(Authentication authentication) {
        return ResponseEntity.ok(recipeService.getMyRecipes(authentication.getName()));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        recipeService.deleteRecipe(id, email);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponse> updateRecipe(
            @PathVariable Long id,
            @RequestPart("data") RecipeUpdateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication
    ) {
        Recipe recipe=recipeService.updateRecipe(id, request, files, authentication.getName());
        return ResponseEntity.ok(recipeService.mapToRecipeResponse(recipe,"LOCAL"));
    }
}
