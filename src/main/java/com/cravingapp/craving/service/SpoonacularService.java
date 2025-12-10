package com.cravingapp.craving.service;

import com.cravingapp.craving.model.SpoonacularDetail;
import com.cravingapp.craving.model.SpoonacularResult;
import com.cravingapp.craving.service.RecipeService.RecipeResponse; // Importă DTO-ul tău principal
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpoonacularService {

    @Value("${spoonacular.api.key}")
    private String apiKey;

    @Value("${spoonacular.base-url}")
    private String baseUrl;

    // Clientul HTTP
    private final RestClient restClient = RestClient.create();

    public List<RecipeResponse> searchRecipesByIngredients(List<String> ingredients,int limit) {

        // 1. Pregătim lista de ingrediente pentru URL (ex: "apples,+flour,+sugar")
        String ingredientsString = String.join(",+", ingredients);

        // 2. Facem cererea către Spoonacular
        // Endpoint: /findByIngredients
        SpoonacularResult[] results = restClient.get()
                .uri(baseUrl + "/findByIngredients?ingredients=" + ingredientsString + "&number="+limit +"&apiKey="+ apiKey)
                .retrieve()
                .body(SpoonacularResult[].class);

        // 3. Convertim rezultatele în formatul aplicației tale (RecipeResponse)
        if (results == null) return new ArrayList<>();

        return List.of(results).stream()
                .map(this::convertToRecipeResponse)
                .collect(Collectors.toList());
    }

    // Funcție de conversie: SpoonacularResult -> RecipeResponse
    private RecipeResponse convertToRecipeResponse(SpoonacularResult spoonData) {

        // Combinăm ingredientele (cele pe care le ai + cele care lipsesc)
        List<String> allIngredients = new ArrayList<>();
        if (spoonData.getUsedIngredients() != null) {
            spoonData.getUsedIngredients().forEach(i -> allIngredients.add(i.getOriginal()));
        }
        if (spoonData.getMissedIngredients() != null) {
            spoonData.getMissedIngredients().forEach(i -> allIngredients.add(i.getOriginal()));
        }

        // Returnăm DTO-ul tău standard
        return new RecipeResponse(
                spoonData.getId(), // ID-ul de la Spoonacular
                spoonData.getTitle(),
                "Rețetă descoperită prin Spoonacular", // Descriere generică (Spoonacular nu dă descriere la search)
                0, // Prep time (nu e disponibil la search simplu)
                0, // Cook time
                0, // Servings
                new ArrayList<>(),

                // Steps (trebuie un apel separat pt asta, lăsăm gol momentan)
                List.of(spoonData.getImage()), // Imaginea
                null,//ingredients
                "SPOONACULAR"// Map-ul de ingrediente complexe (lăsăm null, folosim lista de stringuri dacă ai modificat DTO-ul)
                // SAU dacă ai lista de string-uri în DTO-ul tău:
                // allIngredients
        );
    }
    // În SpoonacularService.java

    public RecipeResponse getRecipeDetails(Long spoonacularId) {
        // Apelăm endpoint-ul complet: /recipes/{id}/information
        SpoonacularDetail detail = restClient.get()
                .uri(baseUrl + "/" + spoonacularId + "/information?apiKey=" + apiKey)
                .retrieve()
                .body(SpoonacularDetail.class);

        if (detail == null) throw new RuntimeException("Not found on Spoonacular");

        // Convertim detaliile (inclusiv pașii!)
        return convertDetailToResponse(detail);
    }

    private RecipeResponse convertDetailToResponse(SpoonacularDetail detail) {
        List<String> steps = new ArrayList<>();

        // Spoonacular are pașii în "analyzedInstructions" -> "steps"
        if (detail.getAnalyzedInstructions() != null && !detail.getAnalyzedInstructions().isEmpty()) {
            detail.getAnalyzedInstructions().get(0).getSteps()
                    .forEach(s -> steps.add(s.getStep())); // Extragem textul pasului
        }

        return new RecipeResponse(
                detail.getId(),
                detail.getTitle(),
                detail.getSummary().replaceAll("<[^>]*>", ""), // Curățăm HTML-ul
                detail.getReadyInMinutes(), // Prep
                0, // Cook (Spoonacular dă doar totalul)
                detail.getServings(),
                steps, // <--- LISTA DE PAȘI ESTE AICI!
                List.of(detail.getImage()),
                // ... (ingrediente)
                new ArrayList<>(),
                "SPOONACULAR"
        );
    }
}