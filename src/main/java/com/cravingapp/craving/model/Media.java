package com.cravingapp.craving.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@Table(name="\"media\"")
public class Media {
    @Id
    @Column(name="media_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "recipe_id",nullable = false)
    private Recipe recipe;
    @Column(name="media_type",nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;
    @Column
    private String url;
    @Column
    private Boolean is_cover;

}
