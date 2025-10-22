package com.banque.situationbancaire.entity;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;

/**
 * Entité représentant les permissions d'actions par rôle
 */
@Entity
@Table(name = "action_role", indexes = {
    @Index(name = "idx_action_role_role_table", columnList = "role_requis,nom_table"),
    @Index(name = "idx_action_role_table_action", columnList = "nom_table,action_autorisee")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionRole implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_action_role")
    private Integer idActionRole;

    @Column(name = "nom_table", nullable = false, length = 50)
    @NotBlank(message = "Le nom de la table est obligatoire")
    private String nomTable;

    @Column(name = "action_autorisee", nullable = false, length = 50)
    @NotBlank(message = "L'action autorisée est obligatoire")
    private String actionAutorisee;

    @Column(name = "role_requis", nullable = false)
    @NotNull(message = "Le rôle requis est obligatoire")
    private Integer roleRequis;
}
