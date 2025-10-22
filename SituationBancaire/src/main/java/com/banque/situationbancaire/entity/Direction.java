package com.banque.situationbancaire.entity;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une direction de l'entreprise
 */
@Entity
@Table(name = "direction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direction implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direction")
    private Integer idDirection;

    @Column(name = "niveau", nullable = false)
    @NotNull(message = "Le niveau est obligatoire")
    private Integer niveau;

    @OneToMany(mappedBy = "direction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<Utilisateur> utilisateurs = new ArrayList<>();
}
