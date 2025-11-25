package com.cravingapp.craving.service;


import com.cravingapp.craving.model.Media;
import com.cravingapp.craving.repository.MediaRepo;
import com.cravingapp.craving.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MediaService {
    private final MediaRepo mediaRepo;
    private final UserRepo userRepo;

    @Transactional
    public Media createMedia(Media media) {
        return mediaRepo.save(media);
    }
}
