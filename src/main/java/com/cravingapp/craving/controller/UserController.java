package com.cravingapp.craving.controller;


import com.cravingapp.craving.model.User;
import com.cravingapp.craving.repository.UserRepo;
import com.cravingapp.craving.service.JwtService;
import com.cravingapp.craving.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.cravingapp.craving.service.FileUploadService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FileUploadService fileUploadService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;

    public record LoginRequest(String username, String password) {}
    public record RegisterRequest(String username, String email, String password) {}
    public record UserDto(
            Long id,
            String username,
            String email,
            String bio,
            String profilePictureUrl,
            LocalDateTime createdAt
    ) {}
    public record AuthResponse(
            String token,
            UserDto userDto
    ) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        User savedUser = new User();
        savedUser.setUsername(registerRequest.username);
        savedUser.setEmail(registerRequest.email);
        savedUser.setPassword(registerRequest.password);
        savedUser=userService.createUser(savedUser);
        UserDto userDto = new UserDto(
                (long)savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getBio(),
                savedUser.getProfile_picture_url(),
                savedUser.getCreated_at()
        );
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 1. Spring verifies the password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );


        var user = (User) authentication.getPrincipal();


        String token = jwtService.generateToken(user);
        UserDto userDto = new UserDto((long) user.getId(),user.getUsername(),user.getEmail(),user.getBio(),user.getProfile_picture_url(),user.getCreated_at());

//        System.out.println(userDto.profilPictureUrl);
        return ResponseEntity.ok(new AuthResponse(token,userDto));
    }
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        // Spring Security a decodat deja token-ul și a pus userul în 'authentication'
        String username = authentication.getName();
//        System.out.println(username);

        // Folosește service-ul pentru a găsi detaliile complete
        User user = userService.findUserByUsername(username).orElseThrow(()->new RuntimeException("No username found"));
        UserDto userProfile= new UserDto((long) user.getId(),user.getUsername(),user.getEmail(),user.getBio(),user.getProfile_picture_url(),user.getCreated_at());
//        System.out.println(userProfile.profilePictureUrl);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping(value = "/update",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> updateProfile(Authentication authentication,@RequestParam(value = "username",required = false) String username,@RequestParam(value = "bio",required = false) String bio, @RequestParam(value = "profileImage",required = false) MultipartFile profilePictureUrl) {
        User user = new User();
        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            String imageUrl = fileUploadService.upload(profilePictureUrl).get("url");
            user.setProfile_picture_url(imageUrl);
        }
        User oldUser = userRepo.findByUsername(authentication.getName()).orElseThrow(()->new RuntimeException("not found User"));
        Integer id = oldUser.getId();

        user.setUsername(username);
        user.setBio(bio);
//        user.setProfile_picture_url(pictureUrl);
        User newUser = userService.updateUser(id, user);
        UserDto userProfile= new UserDto((long) newUser.getId(),newUser.getUsername(),newUser.getEmail(),newUser.getBio(),newUser.getProfile_picture_url(),newUser.getCreated_at());
        return ResponseEntity.ok(userProfile);
    }
}
