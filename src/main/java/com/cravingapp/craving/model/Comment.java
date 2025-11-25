package com.cravingapp.craving.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@Table(name = "comment")
public class Comment {

//    public Integer getCommentId() {
//        return commentId;
//    }
//
//    public void setCommentId(Integer commentId) {
//        this.commentId = commentId;
//    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "comment_id")
    private Integer commentId;

//    public Integer getUserId() {
//        return userId;
//    }
//
//    public void setUserId(Integer userId) {
//        this.userId = userId;
//    }
//
//    public Integer getRecipeId() {
//        return recipeId;
//    }

//    public void setRecipeId(Integer recipeId) {
//        this.recipeId = recipeId;
//    }
//
//    public String getContent() {
//        return content;
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//    }

    @Column(name = "user_id")
    private Integer userId;

    @Column (name = "recipe_id")
    private Integer recipeId;

    @Column(name = "content")
    private String content;

//    public Date getCreatedAt() {
//        return createdAt;
//    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Date createdAt;
}
