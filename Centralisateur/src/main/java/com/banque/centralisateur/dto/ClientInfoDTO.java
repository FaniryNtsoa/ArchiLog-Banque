package com.banque.centralisateur.dto;

import java.time.LocalDate;

/**
 * DTO pour les informations client
 */
public class ClientInfoDTO {
    
    private Long idClient;
    private String numeroClient;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private LocalDate dateNaissance;
    private String numCin;
    private String situationFamiliale;
    private String statut;
    
    // Constructeurs
    public ClientInfoDTO() {}
    
    public ClientInfoDTO(Long idClient, String numeroClient, String nom, String prenom, 
                        String email, String telephone, String adresse, LocalDate dateNaissance,
                        String numCin, String situationFamiliale, String statut) {
        this.idClient = idClient;
        this.numeroClient = numeroClient;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.dateNaissance = dateNaissance;
        this.numCin = numCin;
        this.situationFamiliale = situationFamiliale;
        this.statut = statut;
    }

    // Getters et Setters
    public Long getIdClient() { return idClient; }
    public void setIdClient(Long idClient) { this.idClient = idClient; }

    public String getNumeroClient() { return numeroClient; }
    public void setNumeroClient(String numeroClient) { this.numeroClient = numeroClient; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getNumCin() { return numCin; }
    public void setNumCin(String numCin) { this.numCin = numCin; }

    public String getSituationFamiliale() { return situationFamiliale; }
    public void setSituationFamiliale(String situationFamiliale) { this.situationFamiliale = situationFamiliale; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}