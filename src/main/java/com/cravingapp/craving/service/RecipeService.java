package com.cravingapp.craving.service;


import com.cravingapp.craving.controller.RecipeController;
import com.cravingapp.craving.model.Media;
import com.cravingapp.craving.model.MediaType;
import com.cravingapp.craving.model.Recipe;
import com.cravingapp.craving.model.User;
import com.cravingapp.craving.repository.RecipeRepo;
import com.cravingapp.craving.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepo recipeRepo;
    private final UserRepo userRepo;
    private final FileUploadService fileUploadService;

    public record RecipeResponse(
            Long id,
            String title,
            String description,
            Integer prepTimeMinutes,
            Integer cookTimeMinutes,
            Integer servings,
            List<String> steps,
            List<String> images,
            Map<String,Integer> ingredients
    ){};

    @Transactional
    public Recipe createRecipe(Authentication authentication, RecipeController.RecipeCreateRequest request, List<MultipartFile> files) {
        String username = authentication.getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Recipe recipe = new Recipe();
        recipe.setUser(user);
        recipe.setTitle(request.title());
        recipe.setDescription(request.description());
        recipe.setServings(request.servings());
        recipe.setCook_time_minutes(request.cook_time_minutes());
        recipe.setPrep_time_minutes(request.prep_time_minutes());
        if(files!=null && !files.isEmpty()) {
            List<Media> mediaList = new ArrayList<>();
            for (MultipartFile file : files) {
                Map<String,String> uploadResult = fileUploadService.upload(file);
                Media media = new Media();
                media.setRecipe(recipe);
                media.setUrl(uploadResult.get("url"));
                if(Objects.equals(uploadResult.get("type"), "Image")) media.setMediaType(MediaType.IMAGE);
                else media.setMediaType(MediaType.VIDEO);
                mediaList.add(media);
            }
            recipe.setMediaList(mediaList);
        }
        return recipeRepo.save(recipe);
    }
}
