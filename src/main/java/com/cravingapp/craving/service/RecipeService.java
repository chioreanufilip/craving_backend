package com.cravingapp.craving.service;


import com.cravingapp.craving.controller.RecipeController;
import com.cravingapp.craving.model.*;
import com.cravingapp.craving.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cravingapp.craving.model.Step;
import com.cravingapp.craving.controller.RecipeController.FeedPostResponse;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepo recipeRepo;
    private final UserRepo userRepo;
    private final MediaRepo mediaRepo;
    private final FileUploadService fileUploadService;
    private final SpoonacularService spoonacularService;
    private final StepRepo stepRepo;
    private final IngredientRepo ingredientRepo;
    private static final int TARGET_LIST_SIZE = 20;
    public record IngredientDTO(
            String name,
            String quantity,
            String unit
    ){}
    public record RecipeResponse(
            Long id,
            String title,
            String description,
            Integer prepTimeMinutes,
            Integer cookTimeMinutes,
            Integer servings,
            List<String> steps,
            List<String> images,
            List<IngredientDTO> ingredients,
            String source
    ){};
    // Acesta este folosit DOAR la Editare
    public record RecipeEditResponse(
            Long id,
            String title,
            String description,
            Integer prepTimeMinutes,
            Integer cookTimeMinutes,
            Integer servings,
            List<String> steps,
            List<MediaItem> media,
            List<IngredientDTO> ingredients
    ) {
        // Clasă mică internă pentru ID + URL
        public record MediaItem(Long id, String url) {}
    }


    @Transactional
    public RecipeResponse createRecipe(Authentication authentication, RecipeController.RecipeCreateRequest request, List<MultipartFile> files) {
        String username = authentication.getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Recipe recipe = new Recipe();
        recipe.setUser(user);
        recipe.setTitle(request.title());
        recipe.setDescription(request.description());
        recipe.setServings(request.servings());
        recipe.setCookTimeMinutes(request.cook_time_minutes());
        recipe.setPrepTimeMinutes(request.prep_time_minutes());
        if(files!=null && !files.isEmpty()) {
            List<Media> mediaList = new ArrayList<>();
            for (MultipartFile file : files) {
                Map<String,String> uploadResult = fileUploadService.upload(file);
                Media media = new Media();
                media.setRecipe(recipe);
                media.setUrl(uploadResult.get("url"));
                if(Objects.equals(uploadResult.get("type"), "image")) media.setMediaType(MediaType.IMAGE);
                else media.setMediaType(MediaType.VIDEO);
                mediaList.add(media);
            }
            recipe.setMediaList(mediaList);
            // În metoda createRecipe

            if (request.ingredients() != null) {
                List<RecipeIngredient> recipeIngredients = new ArrayList<>();

                for (IngredientDTO ingReq : request.ingredients()) {

                    String cleanName = ingReq.name().trim().toLowerCase();

                    // 1. Găsește sau Creează Ingredientul (Catalog)
                    Ingredient ingredient = ingredientRepo.findIngredientByName(cleanName)
                            .orElseGet(() -> ingredientRepo.save(new Ingredient(cleanName)));

                    // 2. Creează Legătura
                    RecipeIngredient link = new RecipeIngredient();
                    link.setRecipe(recipe);
                    link.setIngredient(ingredient);

                    // 3. Setează datele EXACTE primite de la Android
                    link.setQuantity(ingReq.quantity()); // "200"
                    link.setUnit(ingReq.unit());         // "g"

                    recipeIngredients.add(link);
                }
                recipe.setRecipeIngredients(recipeIngredients);
            }
        }
        recipeRepo.save(recipe);
        if (request.steps() != null && !request.steps().isEmpty()) {
            List<Step> stepList = new ArrayList<>();
            int order = 1; // Contor pentru numărul pasului

            for (String stepDescription : request.steps()) {
                if (stepDescription.isBlank()) continue; // Sărim peste rânduri goale

                Step step = new Step();
                step.setDescription(stepDescription);
                step.setStep_number(order++); // 1, 2, 3...
                step.setRecipe(recipe); // Leagă de părinte!

                stepList.add(step);
            }
            stepRepo.saveAll(stepList);
//            recipe.setSteps(stepList);
        }

        //                new RecipeResponse(recipe.getId(),recipe.getTitle(),recipe.getDescription(),recipe.getPrep_time_minutes(),recipe.getCook_time_minutes(),recipe.getServings(),null,)
        return mapToRecipeResponse(recipe,"LOCAL");
    }

    public List<RecipeResponse> hybridSearch(List<String> ingredients) {
        // 1. Căutăm LOCAL
        List<Recipe> localEntities = recipeRepo.findBestMatchingRecipes(ingredients);

        // Convertim în DTO
        List<RecipeResponse> finalResults = new ArrayList<>(
                localEntities.stream()
                        .map(r -> mapToRecipeResponse(r, "LOCAL"))
                        .toList()
        );

        // 2. Calculăm câte ne mai lipsesc
        int localCount = finalResults.size();
        int neededFromApi = TARGET_LIST_SIZE - localCount;

        // 3. Dacă mai avem loc, chemăm SPOONACULAR
        if (neededFromApi > 0) {
            try {
                List<RecipeResponse> apiResults = spoonacularService
                        .searchRecipesByIngredients(ingredients, neededFromApi); // Cerem doar diferența!

                // Le marcăm sursa (opțional, dar util pentru UI)
//                 apiResults.forEach(r -> r.setSource("SPOONACULAR"));

                // Le adăugăm la lista finală
                finalResults.addAll(apiResults);

            } catch (Exception e) {
                // Dacă pică netul sau API-ul, nu crăpăm totul.
                // Returnăm măcar rețetele locale pe care le-am găsit.
                System.err.println("Spoonacular a eșuat: " + e.getMessage());
            }
        }

        return finalResults;
    }

    public RecipeResponse getLocalRecipe(Long id) {
        Recipe recipe = recipeRepo.findById(id.intValue())
                .orElseThrow(() -> new RuntimeException("Rețeta locală nu a fost găsită (ID: " + id + ")"));
        System.out.println(recipe.getId());
        System.out.println(recipe.getCookTimeMinutes());
        return mapToRecipeResponse(recipe, "LOCAL");
    }

    // --- METODA NOUĂ: Get Spoonacular ---
    public RecipeResponse getSpoonacularRecipe(Long id) {
        // Aici apelăm serviciul extern care face request-ul HTTP
        return spoonacularService.getRecipeDetails(id);
    }
    public RecipeResponse mapToRecipeResponse(Recipe recipe, String source) {
        System.out.println(recipe.getPrepTimeMinutes());
        System.out.println(recipe.getDescription());
        return new RecipeResponse(
                Long.valueOf(recipe.getId()),
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getPrepTimeMinutes(),
                recipe.getCookTimeMinutes(),
                recipe.getServings(),
//                ListStepToMap(recipe),
                stepRepo.getStepByRecipe(recipe).stream().map(Step::getDescription).toList(),
                mediaRepo.getMediaByRecipe(recipe).stream().map(Media::getUrl).toList(),
                ingredientDTOList(recipe.getRecipeIngredients()),
//                recipe
                // ... mapare câmpuri ...
                source // Aici setăm sursa!
        );


}
//    private Map<Integer,String> ListStepToMap(Recipe recipe){
//        List<Step> step = stepRepo.getStepByRecipe(recipe);
//        Map<Integer,String> map = new HashMap<>();
//        step.forEach(x->{map.put(x.getStepId(), x.getDescription());});
//        return new TreeMap<>(map);
//    }
    private List<IngredientDTO> ingredientDTOList(List<RecipeIngredient> ingredients) {
        List<IngredientDTO> ingredientDTOs = new ArrayList<>();
        ingredients.forEach(x->{ingredientDTOs.add(new IngredientDTO(ingredientRepo.findIngredientById(x.getIngredient().getId()).getName(),x.getQuantity(),x.getUnit()));});
        return ingredientDTOs;
    }
    // ... în interiorul clasei RecipeService ...

    // Metoda de mapare privată
    // 1. Modifică metoda getAllRecipes să primească email-ul userului curent
    public List<FeedPostResponse> getAllFeedPosts(String currentUserEmail) {

        // Găsim userul care cere feed-ul (poate fi null dacă nu e logat, dar la tine e obligatoriu)
        User currentUser = userRepo.findByUsername(currentUserEmail).orElse(null);

        return recipeRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(recipe -> mapToFeedPostResponse(recipe, currentUser))
                .toList();
    }

    // 2. Metoda de mapare care construiește obiectul final
    private FeedPostResponse mapToFeedPostResponse(Recipe recipe, User currentUser) {

        // A. Imaginea de copertă
        String coverUrl = recipe.getMediaList().stream()
//                .filter(m -> "IMAGE".equalsIgnoreCase(m.getMediaType().toString())) // Doar imagini
                .map(Media::getUrl)
                .findFirst()
                .orElse(null);

        // B. User Avatar & Username
        String username = (recipe.getUser() != null) ? recipe.getUser().getUsername() : "Unknown";
        String avatarUrl = (recipe.getUser() != null) ? recipe.getUser().getProfile_picture_url() : null;

        // C. Calcule
        int likesCount = (recipe.getLikes() != null)
                ? (int) recipe.getLikes().stream()
                .filter(l -> l.getReactionType() == ReactionType.LIKE)
                .count()
                : 0;
        int commentsCount = (recipe.getComments() != null) ? recipe.getComments().size() : 0;

        // D. Verificăm "is_Liked"
        boolean isLikedByMe = false;
        if (currentUser != null && recipe.getLikes() != null) {
            isLikedByMe = recipe.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUser.getId())
                            && like.getReactionType() == ReactionType.LIKE);
        }

        // E. Returnăm obiectul gata făcut
        return new FeedPostResponse(
                recipe.getId(),
                recipe.getTitle(),
                username,
                avatarUrl,
                coverUrl,
                likesCount,
                commentsCount,
                isLikedByMe, // Valoarea booleană calculată
                "LOCAL"
        );
    }
    public List<FeedPostResponse> getMyRecipes(String email){
        User user = userRepo.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return recipeRepo.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(recipe -> mapToFeedPostResponse(recipe, user))
                .toList();
    }
    // Adaugă această metodă
    public void deleteRecipe(Long recipeId, String currentUserEmail) {
        // 1. Căutăm rețeta
        Recipe recipe = recipeRepo.findById(recipeId.intValue())
                .orElseThrow(() -> new RuntimeException("Rețeta nu există"));

        // 2. VERIFICARE DE SECURITATE CRITICĂ
        // Verificăm dacă cel care cere ștergerea este proprietarul rețetei
        if (!recipe.getUser().getUsername().equals(currentUserEmail)) { // Sau compară ID-urile
            throw new RuntimeException("Nu ai dreptul să ștergi această rețetă!");
        }

        // 3. Ștergem (Hibernate va șterge automat și pozele/ingredientele datorită CascadeType.ALL)
        recipeRepo.delete(recipe);
    }
    @Transactional
    public Recipe updateRecipe(Long recipeId, RecipeController.RecipeUpdateRequest request, List<MultipartFile> files, String userEmail) {

        Recipe recipe = recipeRepo.findById(recipeId.intValue())
                .orElseThrow(() -> new RuntimeException("Rețeta nu există"));

        if (!recipe.getUser().getUsername().equals(userEmail)) {
            throw new RuntimeException("Nu poți modifica rețeta altcuiva!");
        }

        // --- 1. UPDATE CÂMPURI SIMPLE ---
        recipe.setTitle(request.title());
        recipe.setDescription(request.description());
        recipe.setPrepTimeMinutes(request.prepTimeMinutes());
        recipe.setCookTimeMinutes(request.cookTimeMinutes());
        recipe.setServings(request.servings());

        // --- 2. GESTIONARE MEDIA (POZE/VIDEO) ---

        // A. ȘTERGERE (Poze vechi)
        if (request.mediaIdsToDelete() != null && !request.mediaIdsToDelete().isEmpty()) {
            recipe.getMediaList().removeIf(media -> {
                if (request.mediaIdsToDelete().contains(Long.valueOf(media.getId()))) {
                    // Ștergem fișierul fizic de pe Cloudinary
                    fileUploadService.deleteFile(media.getUrl());
                    return true; // Hibernate va șterge rândul din DB datorită orphanRemoval=true
                }
                return false;
            });
        }

        // B. ADĂUGARE (Poze noi)
        // REPARAȚIE: Verificăm fișierele independent de restul logicii
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                Map<String, String> uploadResult = fileUploadService.upload(file);

                Media media = new Media();
                media.setRecipe(recipe); // Setăm părintele
                media.setUrl(uploadResult.get("url"));

                if ("image".equals(uploadResult.get("type"))) {
                    media.setMediaType(MediaType.IMAGE);
                } else {
                    media.setMediaType(MediaType.VIDEO);
                }

                // REPARAȚIE CRITICĂ: Adăugăm la lista existentă, NU o suprascriem!
                recipe.getMediaList().add(media);
            }
        }

        // --- 3. UPDATE INGREDIENTE ---
        // REPARAȚIE: Logica asta trebuie să fie aici, nu într-un if legat de fișiere
        if (request.ingredients() != null) {
            // Golim lista veche (Hibernate va șterge rândurile vechi)
            recipe.getRecipeIngredients().clear();

            for (IngredientDTO ingReq : request.ingredients()) {
                String cleanName = ingReq.name().trim().toLowerCase();

                // Găsește sau Creează Ingredientul în catalog
                Ingredient ingredient = ingredientRepo.findIngredientByName(cleanName)
                        .orElseGet(() -> ingredientRepo.save(new Ingredient(cleanName)));

                // Creează Legătura
                RecipeIngredient link = new RecipeIngredient();
                link.setRecipe(recipe);
                link.setIngredient(ingredient);
                link.setQuantity(ingReq.quantity());
                link.setUnit(ingReq.unit());

                // Adăugăm în lista rețetei (care acum e goală și curată)
                recipe.getRecipeIngredients().add(link);
            }
        }

        // --- 4. UPDATE PAȘI (STEPS) ---
        if (request.steps() != null) {
            // REPARAȚIE: Trebuie să ștergem pașii vechi mai întâi!
            // Presupunând că ai o listă `steps` în Recipe cu orphanRemoval=true
            if (recipe.getSteps() != null) {
                recipe.getSteps().clear();
            } else {
                recipe.setSteps(new ArrayList<>());
            }

            int order = 1;
            for (String stepDescription : request.steps()) {
                if (stepDescription == null || stepDescription.isBlank()) continue;

                Step step = new Step();
                step.setDescription(stepDescription);
                step.setStep_number(order++);
                step.setRecipe(recipe); // Leagă de părinte

                // Adaugă la lista părintelui (Hibernate va face save automat prin Cascade)
                recipe.getSteps().add(step);
            }
        }

        // --- 5. SALVARE FINALĂ ---
        // Un singur save la final este suficient dacă ai CascadeType.ALL setat corect
        return recipeRepo.save(recipe);
    }
}
