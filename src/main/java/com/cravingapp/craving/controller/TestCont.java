package com.cravingapp.craving.controller;
import com.cravingapp.craving.model.Comment;
import com.cravingapp.craving.repository.CommentRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class TestCont {

    @Autowired
    CommentRepo commentRepo;

    @GetMapping("/test")
    public String test() {
        try {
//        Comment comment1 = new Comment();
//        comment1=commentRepo.getById(1);
//        return comment1.getUserId().toString();
        Comment comment = new Comment();
        comment.setContent("This is a test");
//        comment.setUserId(1);
//        comment.setRecipeId(1);
        commentRepo.save(comment);

        return "success";
        }
        catch (Exception e) {
            return "Eroare grava"+e.getMessage();
        }
    }

}
