package com.banque.pret.ejb.remote;

import com.banque.pret.dto.PretDTO;
import com.banque.pret.dto.SimulationPretDTO;
import com.banque.pret.entity.enums.StatutPret;
import jakarta.ejb.Remote;
import java.util.List;

/**
 * Interface remote pour le service de gestion des prêts
 */
@Remote
public interface PretServiceRemote {
    
    /**
     * Simule un prêt et génère le tableau d'amortissement prévisionnel
     * @param simulationDTO Les paramètres de simulation
     * @return La simulation complète avec tableau d'amortissement
     */
    SimulationPretDTO simulerPret(SimulationPretDTO simulationDTO);
    
    /**
     * Crée une demande de prêt
     * @param pretDTO Le prêt à créer
     * @return Le prêt créé avec son ID
     */
    PretDTO creerDemandePret(PretDTO pretDTO);
    
    /**
     * Approuve une demande de prêt
     * @param idPret L'ID du prêt à approuver
     * @return Le prêt approuvé
     */
    PretDTO approuverPret(Long idPret);
    
    /**
     * Refuse une demande de prêt
     * @param idPret L'ID du prêt à refuser
     * @param motifRefus Le motif du refus
     * @return Le prêt refusé
     */
    PretDTO refuserPret(Long idPret, String motifRefus);
    
    /**
     * Recherche un prêt par son ID
     * @param idPret L'ID du prêt
     * @return Le prêt trouvé ou null
     */
    PretDTO rechercherPretParId(Long idPret);
    
    /**
     * Recherche un prêt par son numéro
     * @param numeroPret Le numéro du prêt
     * @return Le prêt trouvé ou null
     */
    PretDTO rechercherPretParNumero(String numeroPret);
    
    /**
     * Liste tous les prêts
     * @return La liste de tous les prêts
     */
    List<PretDTO> listerTousLesPrets();
    
    /**
     * Liste tous les prêts d'un client
     * @param idClient L'ID du client
     * @return La liste des prêts du client
     */
    List<PretDTO> listerPretsParClient(Long idClient);
    
    /**
     * Liste tous les prêts par statut
     * @param statut Le statut recherché
     * @return La liste des prêts avec ce statut
     */
    List<PretDTO> listerPretsParStatut(StatutPret statut);
    
    /**
     * Liste tous les prêts d'un client par statut
     * @param idClient L'ID du client
     * @param statut Le statut recherché
     * @return La liste des prêts
     */
    List<PretDTO> listerPretsParClientEtStatut(Long idClient, StatutPret statut);
    
    /**
     * Supprime un prêt
     * @param idPret L'ID du prêt à supprimer
     */
    void supprimerPret(Long idPret);
}
