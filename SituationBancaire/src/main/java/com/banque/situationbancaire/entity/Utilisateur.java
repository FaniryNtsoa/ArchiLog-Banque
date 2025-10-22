package com.banque.situationbancaire.entity;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;

/**
 * Entité représentant un utilisateur du système
 */
@Entity
@Table(name = "utilisateur", indexes = {
    @Index(name = "idx_utilisateur_login", columnList = "login_utilisateur", unique = true),
    @Index(name = "idx_utilisateur_role", columnList = "role_utilisateur")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Integer idUtilisateur;

    @Column(name = "login_utilisateur", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Le login est obligatoire")
    private String loginUtilisateur;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_direction")
    private Direction direction;

    @Column(name = "role_utilisateur", nullable = false)
    @NotNull(message = "Le rôle est obligatoire")
    private Integer roleUtilisateur;
}
