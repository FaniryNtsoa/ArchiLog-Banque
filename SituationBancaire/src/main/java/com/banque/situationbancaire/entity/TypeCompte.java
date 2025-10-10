package com.banque.situationbancaire.entity;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un type de compte (étudiant, business, standard, premium)
 */
@Entity
@Table(name = "type_compte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeCompte implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_compte")
    private Long idTypeCompte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parametre_actuel", nullable = false)
    @NotNull(message = "Les paramètres du type de compte sont obligatoires")
    @ToString.Exclude
    private ParametresCompte parametreActuel;

    @Column(name = "code_type", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Le code du type de compte est obligatoire")
    private String codeType;

    @Column(name = "libelle", nullable = false, length = 100)
    @NotBlank(message = "Le libellé est obligatoire")
    private String libelle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "typeCompte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<CompteCourant> comptes = new ArrayList<>();
}
