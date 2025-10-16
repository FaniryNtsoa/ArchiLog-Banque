package com.banque.situationbancaire.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour les mouvements bancaires
 */
public class MouvementDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idMouvement;
    private String numeroCompte;
    private BigDecimal montant;
    private String typeOperation;
    private String natureOperation;
    private String categorieOperation;
    private String libelle;
    private LocalDateTime dateOperation;
    private LocalDateTime dateValeur;
    private BigDecimal soldeAvant;
    private BigDecimal soldeApres;
    private String numeroCompteBeneficiaire; // Pour les virements
    private String nomBeneficiaire; // Pour les virements
    private String reference;
    private String statut;
    
    // Constructeurs
    public MouvementDTO() {}
    
    public MouvementDTO(Long idMouvement, String numeroCompte, BigDecimal montant, 
                       String typeOperation, String natureOperation, String categorieOperation,
                       String libelle, LocalDateTime dateOperation, LocalDateTime dateValeur,
                       BigDecimal soldeAvant, BigDecimal soldeApres, String numeroCompteBeneficiaire,
                       String nomBeneficiaire, String reference, String statut) {
        this.idMouvement = idMouvement;
        this.numeroCompte = numeroCompte;
        this.montant = montant;
        this.typeOperation = typeOperation;
        this.natureOperation = natureOperation;
        this.categorieOperation = categorieOperation;
        this.libelle = libelle;
        this.dateOperation = dateOperation;
        this.dateValeur = dateValeur;
        this.soldeAvant = soldeAvant;
        this.soldeApres = soldeApres;
        this.numeroCompteBeneficiaire = numeroCompteBeneficiaire;
        this.nomBeneficiaire = nomBeneficiaire;
        this.reference = reference;
        this.statut = statut;
    }

    // Getters et Setters
    public Long getIdMouvement() { return idMouvement; }
    public void setIdMouvement(Long idMouvement) { this.idMouvement = idMouvement; }

    public String getNumeroCompte() { return numeroCompte; }
    public void setNumeroCompte(String numeroCompte) { this.numeroCompte = numeroCompte; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public String getTypeOperation() { return typeOperation; }
    public void setTypeOperation(String typeOperation) { this.typeOperation = typeOperation; }

    public String getNatureOperation() { return natureOperation; }
    public void setNatureOperation(String natureOperation) { this.natureOperation = natureOperation; }

    public String getCategorieOperation() { return categorieOperation; }
    public void setCategorieOperation(String categorieOperation) { this.categorieOperation = categorieOperation; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public LocalDateTime getDateOperation() { return dateOperation; }
    public void setDateOperation(LocalDateTime dateOperation) { this.dateOperation = dateOperation; }

    public LocalDateTime getDateValeur() { return dateValeur; }
    public void setDateValeur(LocalDateTime dateValeur) { this.dateValeur = dateValeur; }

    public BigDecimal getSoldeAvant() { return soldeAvant; }
    public void setSoldeAvant(BigDecimal soldeAvant) { this.soldeAvant = soldeAvant; }

    public BigDecimal getSoldeApres() { return soldeApres; }
    public void setSoldeApres(BigDecimal soldeApres) { this.soldeApres = soldeApres; }

    public String getNumeroCompteBeneficiaire() { return numeroCompteBeneficiaire; }
    public void setNumeroCompteBeneficiaire(String numeroCompteBeneficiaire) { this.numeroCompteBeneficiaire = numeroCompteBeneficiaire; }

    public String getNomBeneficiaire() { return nomBeneficiaire; }
    public void setNomBeneficiaire(String nomBeneficiaire) { this.nomBeneficiaire = nomBeneficiaire; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "MouvementDTO{" +
                "idMouvement=" + idMouvement +
                ", numeroCompte='" + numeroCompte + '\'' +
                ", montant=" + montant +
                ", typeOperation='" + typeOperation + '\'' +
                ", libelle='" + libelle + '\'' +
                ", dateOperation=" + dateOperation +
                '}';
    }
}