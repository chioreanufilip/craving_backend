package com.cravingapp.craving.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "\"like\"") // "like" e cuvânt rezervat SQL, sper că tabelul se cheamă "likes"
@Data
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;
}
