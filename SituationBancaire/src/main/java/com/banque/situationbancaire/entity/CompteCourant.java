package com.banque.situationbancaire.entity;

import com.banque.situationbancaire.entity.enums.StatutCompte;
import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un compte courant
 */
@Entity
@Table(name = "compte_courant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteCourant implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compte")
    private Long idCompte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client", nullable = false)
    @NotNull(message = "Le client est obligatoire")
    @ToString.Exclude
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_compte", nullable = false)
    @NotNull(message = "Le type de compte est obligatoire")
    @ToString.Exclude
    private TypeCompte typeCompte;

    @Column(name = "numero_compte", unique = true, nullable = false, length = 30)
    @NotBlank(message = "Le numéro de compte est obligatoire")
    private String numeroCompte;

    @Column(name = "libelle_compte", length = 100)
    private String libelleCompte;

    @Column(name = "devise", length = 3)
    @Builder.Default
    private String devise = "XOF";

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    @Builder.Default
    private StatutCompte statut = StatutCompte.OUVERT;

    @Column(name = "date_ouverture", nullable = false)
    @NotNull(message = "La date d'ouverture est obligatoire")
    private LocalDate dateOuverture;

    @Column(name = "date_fermeture")
    private LocalDate dateFermeture;

    @Column(name = "motif_fermeture", length = 255)
    private String motifFermeture;

    @OneToMany(mappedBy = "compte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<Mouvement> mouvements = new ArrayList<>();

    @OneToMany(mappedBy = "compte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<InteretJournalier> interets = new ArrayList<>();

    @OneToMany(mappedBy = "compte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<FraisTenueHistorique> fraisTenus = new ArrayList<>();
}
