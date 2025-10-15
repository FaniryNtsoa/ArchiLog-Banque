package com.banque.centralisateur.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour les informations de compte courant
 */
public class CompteInfoDTO {
    
    private Long idCompte;
    private String numeroCompte;
    private BigDecimal soldeInitial;
    private BigDecimal soldeActuel;
    private String statutCompte;
    private LocalDateTime dateCreation;
    private String typeCompte;
    
    // Informations client
    private Long idClient;
    private String numeroClient;
    private String nomClient;
    private String prenomClient;
    private String emailClient;
    
    // Constructeurs
    public CompteInfoDTO() {}
    
    public CompteInfoDTO(Long idCompte, String numeroCompte, BigDecimal soldeInitial, 
                        BigDecimal soldeActuel, String statutCompte, LocalDateTime dateCreation,
                        String typeCompte, Long idClient, String numeroClient,
                        String nomClient, String prenomClient, String emailClient) {
        this.idCompte = idCompte;
        this.numeroCompte = numeroCompte;
        this.soldeInitial = soldeInitial;
        this.soldeActuel = soldeActuel;
        this.statutCompte = statutCompte;
        this.dateCreation = dateCreation;
        this.typeCompte = typeCompte;
        this.idClient = idClient;
        this.numeroClient = numeroClient;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.emailClient = emailClient;
    }

    // Getters et Setters
    public Long getIdCompte() { return idCompte; }
    public void setIdCompte(Long idCompte) { this.idCompte = idCompte; }

    public String getNumeroCompte() { return numeroCompte; }
    public void setNumeroCompte(String numeroCompte) { this.numeroCompte = numeroCompte; }

    public BigDecimal getSoldeInitial() { return soldeInitial; }
    public void setSoldeInitial(BigDecimal soldeInitial) { this.soldeInitial = soldeInitial; }

    public BigDecimal getSoldeActuel() { return soldeActuel; }
    public void setSoldeActuel(BigDecimal soldeActuel) { this.soldeActuel = soldeActuel; }

    public String getStatutCompte() { return statutCompte; }
    public void setStatutCompte(String statutCompte) { this.statutCompte = statutCompte; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getTypeCompte() { return typeCompte; }
    public void setTypeCompte(String typeCompte) { this.typeCompte = typeCompte; }

    public Long getIdClient() { return idClient; }
    public void setIdClient(Long idClient) { this.idClient = idClient; }

    public String getNumeroClient() { return numeroClient; }
    public void setNumeroClient(String numeroClient) { this.numeroClient = numeroClient; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getPrenomClient() { return prenomClient; }
    public void setPrenomClient(String prenomClient) { this.prenomClient = prenomClient; }

    public String getEmailClient() { return emailClient; }
    public void setEmailClient(String emailClient) { this.emailClient = emailClient; }
}