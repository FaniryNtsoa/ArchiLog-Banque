package com.banque.situationbancaire.entity;

import com.banque.situationbancaire.entity.enums.CategorieOperation;
import com.banque.situationbancaire.entity.enums.NatureOperation;
import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un type d'opération bancaire
 */
@Entity
@Table(name = "type_operation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeOperation implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_operation")
    private Long idTypeOperation;

    @Column(name = "code_operation", unique = true, nullable = false, length = 10)
    @NotBlank(message = "Le code opération est obligatoire")
    private String codeOperation;

    @Column(name = "libelle_operation", nullable = false, length = 100)
    @NotBlank(message = "Le libellé de l'opération est obligatoire")
    private String libelleOperation;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false)
    @NotNull(message = "La catégorie est obligatoire")
    private CategorieOperation categorie;

    @Enumerated(EnumType.STRING)
    @Column(name = "nature")
    private NatureOperation nature;

    @OneToMany(mappedBy = "typeOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<Mouvement> mouvements = new ArrayList<>();
}
