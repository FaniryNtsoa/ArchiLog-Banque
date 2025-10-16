package com.banque.situationbancaire.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour les informations de compte courant
 */
public class CompteCourantDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idCompte;
    private String numeroCompte;
    private String libelleCompte;
    private BigDecimal solde;
    private BigDecimal soldeInitial;
    private BigDecimal decouvertAutorise;
    private String statut;
    private String devise;
    private LocalDateTime dateOuverture;
    private LocalDateTime dateFermeture;
    private String motifFermeture;
    private Long idClient;
    private String numeroClient; // Pour faciliter l'affichage
    private String nomClient; // Pour faciliter l'affichage
    private String prenomClient; // Pour faciliter l'affichage
    private String typeCompte;
    private List<MouvementDTO> mouvements;
    
    // Constructeurs
    public CompteCourantDTO() {}
    
    public CompteCourantDTO(Long idCompte, String numeroCompte, BigDecimal solde, 
                           BigDecimal decouvertAutorise, String statut, 
                           LocalDateTime dateOuverture, LocalDateTime dateFermeture,
                           Long idClient, String numeroClient, String nomClient, 
                           String prenomClient, String typeCompte) {
        this.idCompte = idCompte;
        this.numeroCompte = numeroCompte;
        this.solde = solde;
        this.decouvertAutorise = decouvertAutorise;
        this.statut = statut;
        this.dateOuverture = dateOuverture;
        this.dateFermeture = dateFermeture;
        this.idClient = idClient;
        this.numeroClient = numeroClient;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.typeCompte = typeCompte;
    }

    // Getters et Setters
    public Long getIdCompte() { return idCompte; }
    public void setIdCompte(Long idCompte) { this.idCompte = idCompte; }

    public String getNumeroCompte() { return numeroCompte; }
    public void setNumeroCompte(String numeroCompte) { this.numeroCompte = numeroCompte; }

    public BigDecimal getSolde() { return solde; }
    public void setSolde(BigDecimal solde) { this.solde = solde; }

    public BigDecimal getDecouvertAutorise() { return decouvertAutorise; }
    public void setDecouvertAutorise(BigDecimal decouvertAutorise) { this.decouvertAutorise = decouvertAutorise; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateOuverture() { return dateOuverture; }
    public void setDateOuverture(LocalDateTime dateOuverture) { this.dateOuverture = dateOuverture; }

    public LocalDateTime getDateFermeture() { return dateFermeture; }
    public void setDateFermeture(LocalDateTime dateFermeture) { this.dateFermeture = dateFermeture; }

    public Long getIdClient() { return idClient; }
    public void setIdClient(Long idClient) { this.idClient = idClient; }

    public String getNumeroClient() { return numeroClient; }
    public void setNumeroClient(String numeroClient) { this.numeroClient = numeroClient; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getPrenomClient() { return prenomClient; }
    public void setPrenomClient(String prenomClient) { this.prenomClient = prenomClient; }

    public String getTypeCompte() { return typeCompte; }
    public void setTypeCompte(String typeCompte) { this.typeCompte = typeCompte; }

    public List<MouvementDTO> getMouvements() { return mouvements; }
    public void setMouvements(List<MouvementDTO> mouvements) { this.mouvements = mouvements; }

    public String getLibelleCompte() { return libelleCompte; }
    public void setLibelleCompte(String libelleCompte) { this.libelleCompte = libelleCompte; }

    public BigDecimal getSoldeInitial() { return soldeInitial; }
    public void setSoldeInitial(BigDecimal soldeInitial) { this.soldeInitial = soldeInitial; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public String getMotifFermeture() { return motifFermeture; }
    public void setMotifFermeture(String motifFermeture) { this.motifFermeture = motifFermeture; }

    @Override
    public String toString() {
        return "CompteCourantDTO{" +
                "idCompte=" + idCompte +
                ", numeroCompte='" + numeroCompte + '\'' +
                ", solde=" + solde +
                ", statut='" + statut + '\'' +
                ", idClient=" + idClient +
                ", numeroClient='" + numeroClient + '\'' +
                '}';
    }
}