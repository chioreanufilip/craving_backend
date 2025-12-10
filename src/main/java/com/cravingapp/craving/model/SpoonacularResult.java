package com.cravingapp.craving.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoonacularResult {
    private Long id;
    private String title;
    private String image;
    private Integer likes;

    // Spoonacular ne dă ingredientele în două liste: folosite și lipsă
    private List<SpoonIngredient> usedIngredients;
    private List<SpoonIngredient> missedIngredients;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SpoonIngredient {
        private String original; // Ex: "2 cups of flour"
        private String name;     // Ex: "flour"
    }
}
