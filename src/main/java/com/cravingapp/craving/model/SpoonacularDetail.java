package com.cravingapp.craving.model;
//package com.cravingapp.craving.dto.spoonacular;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Ignoră sutele de câmpuri inutile de la Spoonacular
public class SpoonacularDetail {

    private Long id;
    private String title;
    private String image;
    private String summary; // Descrierea (poate conține HTML)
    private Integer readyInMinutes; // Timp total
    private Integer servings;

    // --- PAȘII DE PREPARARE ---
    // Spoonacular are o structură ciudată: O listă de instrucțiuni,
    // care conține o listă de pași.
    private List<AnalyzedInstruction> analyzedInstructions;

    // --- INGREDIENTELE ---
    private List<ExtendedIngredient> extendedIngredients;

    // --- CLASE INTERNE PENTRU STRUCTURA JSON ---

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnalyzedInstruction {
        private List<Step> steps;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {
        private Integer number;
        private String step; // Textul pasului
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtendedIngredient {
        private Long id;
        private String name;
        private String original; // Textul complet (ex: "2 cups of flour")
        private Double amount;
        private String unit;
    }
}
