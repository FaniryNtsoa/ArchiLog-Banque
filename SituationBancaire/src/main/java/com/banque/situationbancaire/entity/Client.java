package com.banque.situationbancaire.entity;

import com.banque.situationbancaire.entity.enums.SituationFamiliale;
import com.banque.situationbancaire.entity.enums.StatutClient;
import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un client de la banque
 */
@Entity
@Table(name = "client")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_client")
    private Long idClient;

    @Column(name = "numero_client", unique = true, nullable = false, length = 20)
    private String numeroClient;

    @Column(name = "nom", nullable = false, length = 100)
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Column(name = "date_naissance", nullable = false)
    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @Column(name = "num_cin", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Le numéro CIN est obligatoire")
    private String numCin;

    @Column(name = "email", unique = true, nullable = false, length = 150)
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "adresse", length = 255)
    private String adresse;

    @Column(name = "code_postal", length = 10)
    private String codePostal;

    @Column(name = "ville", length = 100)
    private String ville;

    @Column(name = "profession", length = 100)
    private String profession;

    @Column(name = "revenu_mensuel", precision = 15, scale = 2)
    private BigDecimal revenuMensuel;

    @Column(name = "solde_initial", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal soldeInitial = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "situation_familiale")
    private SituationFamiliale situationFamiliale;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    @Builder.Default
    private StatutClient statut = StatutClient.ACTIF;

    @Column(name = "date_creation", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_modification")
    @Builder.Default
    private LocalDateTime dateModification = LocalDateTime.now();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<CompteCourant> comptes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.numeroClient == null || this.numeroClient.isEmpty()) {
            this.numeroClient = generateNumeroClient();
        }
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    /**
     * Génère un numéro de client automatiquement
     * Format: CLI + timestamp + 4 chiffres aléatoires
     */
    private String generateNumeroClient() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 9999);
        return String.format("CLI%d%04d", timestamp, random);
    }
}
