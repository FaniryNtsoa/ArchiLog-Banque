package com.banque.situationbancaire.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour les virements
 */
public class VirementDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idVirement;
    private String numeroCompteDebiteur;
    private String numeroCompteCrediteur;
    private BigDecimal montant;
    private String libelle;
    private String reference;
    private LocalDateTime dateVirement;
    private LocalDateTime dateExecution;
    private String statut;
    private String motifRejet;
    
    // Informations additionnelles pour l'affichage
    private String nomDebiteur;
    private String nomCrediteur;
    
    // Constructeurs
    public VirementDTO() {}
    
    public VirementDTO(Long idVirement, String numeroCompteDebiteur, String numeroCompteCrediteur,
                      BigDecimal montant, String libelle, String reference, 
                      LocalDateTime dateVirement, LocalDateTime dateExecution, 
                      String statut, String motifRejet, String nomDebiteur, String nomCrediteur) {
        this.idVirement = idVirement;
        this.numeroCompteDebiteur = numeroCompteDebiteur;
        this.numeroCompteCrediteur = numeroCompteCrediteur;
        this.montant = montant;
        this.libelle = libelle;
        this.reference = reference;
        this.dateVirement = dateVirement;
        this.dateExecution = dateExecution;
        this.statut = statut;
        this.motifRejet = motifRejet;
        this.nomDebiteur = nomDebiteur;
        this.nomCrediteur = nomCrediteur;
    }

    // Getters et Setters
    public Long getIdVirement() { return idVirement; }
    public void setIdVirement(Long idVirement) { this.idVirement = idVirement; }

    public String getNumeroCompteDebiteur() { return numeroCompteDebiteur; }
    public void setNumeroCompteDebiteur(String numeroCompteDebiteur) { this.numeroCompteDebiteur = numeroCompteDebiteur; }

    public String getNumeroCompteCrediteur() { return numeroCompteCrediteur; }
    public void setNumeroCompteCrediteur(String numeroCompteCrediteur) { this.numeroCompteCrediteur = numeroCompteCrediteur; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public LocalDateTime getDateVirement() { return dateVirement; }
    public void setDateVirement(LocalDateTime dateVirement) { this.dateVirement = dateVirement; }

    public LocalDateTime getDateExecution() { return dateExecution; }
    public void setDateExecution(LocalDateTime dateExecution) { this.dateExecution = dateExecution; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getMotifRejet() { return motifRejet; }
    public void setMotifRejet(String motifRejet) { this.motifRejet = motifRejet; }

    public String getNomDebiteur() { return nomDebiteur; }
    public void setNomDebiteur(String nomDebiteur) { this.nomDebiteur = nomDebiteur; }

    public String getNomCrediteur() { return nomCrediteur; }
    public void setNomCrediteur(String nomCrediteur) { this.nomCrediteur = nomCrediteur; }

    @Override
    public String toString() {
        return "VirementDTO{" +
                "idVirement=" + idVirement +
                ", numeroCompteDebiteur='" + numeroCompteDebiteur + '\'' +
                ", numeroCompteCrediteur='" + numeroCompteCrediteur + '\'' +
                ", montant=" + montant +
                ", statut='" + statut + '\'' +
                ", dateVirement=" + dateVirement +
                '}';
    }
}