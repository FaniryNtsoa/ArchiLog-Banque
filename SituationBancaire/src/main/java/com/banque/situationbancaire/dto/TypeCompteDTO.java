package com.banque.situationbancaire.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO pour les informations de type de compte
 */
public class TypeCompteDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idTypeCompte;
    private String codeType;
    private String libelle;
    private String description;
    
    // Informations des paramètres associés
    private Long idParametre;
    private BigDecimal plafondRetraitJournalier;
    private BigDecimal plafondVirementJournalier;
    private BigDecimal montantDecouvertAutorise;
    private BigDecimal fraisTenueCompte;
    private String fraisTenueComptePeriodicite;
    
    // Informations du taux de découvert
    private BigDecimal tauxAnnuel;
    private String descriptionTaux;
    
    // Constructeurs
    public TypeCompteDTO() {}
    
    public TypeCompteDTO(Long idTypeCompte, String codeType, String libelle, String description,
                        Long idParametre, BigDecimal plafondRetraitJournalier, 
                        BigDecimal plafondVirementJournalier, BigDecimal montantDecouvertAutorise,
                        BigDecimal fraisTenueCompte, String fraisTenueComptePeriodicite,
                        BigDecimal tauxAnnuel, String descriptionTaux) {
        this.idTypeCompte = idTypeCompte;
        this.codeType = codeType;
        this.libelle = libelle;
        this.description = description;
        this.idParametre = idParametre;
        this.plafondRetraitJournalier = plafondRetraitJournalier;
        this.plafondVirementJournalier = plafondVirementJournalier;
        this.montantDecouvertAutorise = montantDecouvertAutorise;
        this.fraisTenueCompte = fraisTenueCompte;
        this.fraisTenueComptePeriodicite = fraisTenueComptePeriodicite;
        this.tauxAnnuel = tauxAnnuel;
        this.descriptionTaux = descriptionTaux;
    }

    // Getters et Setters
    public Long getIdTypeCompte() { return idTypeCompte; }
    public void setIdTypeCompte(Long idTypeCompte) { this.idTypeCompte = idTypeCompte; }

    public String getCodeType() { return codeType; }
    public void setCodeType(String codeType) { this.codeType = codeType; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getIdParametre() { return idParametre; }
    public void setIdParametre(Long idParametre) { this.idParametre = idParametre; }

    public BigDecimal getPlafondRetraitJournalier() { return plafondRetraitJournalier; }
    public void setPlafondRetraitJournalier(BigDecimal plafondRetraitJournalier) { 
        this.plafondRetraitJournalier = plafondRetraitJournalier; 
    }

    public BigDecimal getPlafondVirementJournalier() { return plafondVirementJournalier; }
    public void setPlafondVirementJournalier(BigDecimal plafondVirementJournalier) { 
        this.plafondVirementJournalier = plafondVirementJournalier; 
    }

    public BigDecimal getMontantDecouvertAutorise() { return montantDecouvertAutorise; }
    public void setMontantDecouvertAutorise(BigDecimal montantDecouvertAutorise) { 
        this.montantDecouvertAutorise = montantDecouvertAutorise; 
    }

    public BigDecimal getFraisTenueCompte() { return fraisTenueCompte; }
    public void setFraisTenueCompte(BigDecimal fraisTenueCompte) { 
        this.fraisTenueCompte = fraisTenueCompte; 
    }

    public String getFraisTenueComptePeriodicite() { return fraisTenueComptePeriodicite; }
    public void setFraisTenueComptePeriodicite(String fraisTenueComptePeriodicite) { 
        this.fraisTenueComptePeriodicite = fraisTenueComptePeriodicite; 
    }

    public BigDecimal getTauxAnnuel() { return tauxAnnuel; }
    public void setTauxAnnuel(BigDecimal tauxAnnuel) { this.tauxAnnuel = tauxAnnuel; }

    public String getDescriptionTaux() { return descriptionTaux; }
    public void setDescriptionTaux(String descriptionTaux) { this.descriptionTaux = descriptionTaux; }

    @Override
    public String toString() {
        return "TypeCompteDTO{" +
                "idTypeCompte=" + idTypeCompte +
                ", codeType='" + codeType + '\'' +
                ", libelle='" + libelle + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}