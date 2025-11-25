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

import java.util.List;

@RestController
@RequestMapping("api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;
    public record RecipeCreateRequest(String title,String description,Integer prep_time_minutes,Integer cook_time_minutes,Integer servings,List<String> ingredients,List<String> steps) {}

    @PostMapping(value = "/create",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?>createRecipe(Authentication authentication,
                                         @RequestPart("data") RecipeCreateRequest recipeCreateRequest,
                                         @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        Recipe recipe = recipeService.createRecipe(authentication, recipeCreateRequest, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(recipe);
    }

}
