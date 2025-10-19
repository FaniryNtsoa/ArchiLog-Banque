package com.banque.situationbancaire.entity.enums;

/**
 * Énumération représentant le statut d'un découvert
 */
public enum StatutDecouvert {
    /**
     * Découvert actif - en cours d'utilisation
     */
    ACTIF("Actif"),
    
    /**
     * Découvert remboursé - entièrement soldé
     */
    REMBOURSE("Remboursé"),
    
    /**
     * Découvert fermé - clôturé définitivement
     */
    FERME("Fermé"),
    
    /**
     * Découvert suspendu - temporairement inactif
     */
    SUSPENDU("Suspendu");
    
    private final String libelle;
    
    StatutDecouvert(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
    
    @Override
    public String toString() {
        return libelle;
    }
}