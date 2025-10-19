package com.banque.pret.ejb.remote;

import com.banque.pret.dto.EcheanceDTO;
import com.banque.pret.dto.RemboursementDTO;
import jakarta.ejb.Remote;
import java.util.List;

/**
 * Interface remote pour le service de gestion des échéances et remboursements
 */
@Remote
public interface EcheanceServiceRemote {
    
    /**
     * Récupère le tableau d'amortissement d'un prêt
     * @param idPret L'ID du prêt
     * @return La liste des échéances
     */
    List<EcheanceDTO> obtenirTableauAmortissement(Long idPret);
    
    /**
     * Recherche une échéance par son ID
     * @param idEcheance L'ID de l'échéance
     * @return L'échéance trouvée ou null
     */
    EcheanceDTO rechercherEcheanceParId(Long idEcheance);
    
    /**
     * Enregistre un remboursement d'échéance
     * @param remboursementDTO Le remboursement à enregistrer
     * @return Le remboursement enregistré
     */
    RemboursementDTO enregistrerRemboursement(RemboursementDTO remboursementDTO);
    
    /**
     * Liste tous les remboursements d'une échéance
     * @param idEcheance L'ID de l'échéance
     * @return La liste des remboursements
     */
    List<RemboursementDTO> listerRemboursementsParEcheance(Long idEcheance);
    
    /**
     * Liste tous les remboursements d'un prêt
     * @param idPret L'ID du prêt
     * @return La liste des remboursements
     */
    List<RemboursementDTO> listerRemboursementsParPret(Long idPret);
    
    /**
     * Liste les échéances impayées d'un prêt
     * @param idPret L'ID du prêt
     * @return La liste des échéances impayées
     */
    List<EcheanceDTO> listerEcheancesImpayees(Long idPret);
    
    /**
     * Liste les échéances en retard
     * @return La liste des échéances en retard
     */
    List<EcheanceDTO> listerEcheancesEnRetard();
}
