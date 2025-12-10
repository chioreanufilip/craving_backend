package com.cravingapp.craving.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@Table(name="\"step\"")
public class Step {
    @Id
    @Column(name = "step_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int stepId;
    @ManyToOne
    @JoinColumn(name = "recipe_id",nullable = false)
    private Recipe recipe;
    @Column
    private Integer step_number;
    @Column
    private String description;
    @Column
    private String media_url;
}
