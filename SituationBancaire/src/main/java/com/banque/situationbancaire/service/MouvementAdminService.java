package com.banque.situationbancaire.service;

import com.banque.situationbancaire.dto.MouvementDTO;
import com.banque.situationbancaire.entity.Mouvement;
import com.banque.situationbancaire.entity.CompteCourant;
import com.banque.situationbancaire.entity.TypeOperation;
import com.banque.situationbancaire.repository.MouvementRepository;
import com.banque.situationbancaire.repository.CompteCourantRepository;
import com.banque.situationbancaire.repository.TypeOperationRepository;
import com.banque.situationbancaire.ejb.remote.UserSessionBeanRemote;
import com.banque.situationbancaire.ejb.remote.MouvementAdminServiceRemote;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service de gestion des mouvements avec vérification des permissions
 */
@Stateless
public class MouvementAdminService implements MouvementAdminServiceRemote {

    @EJB
    private MouvementRepository mouvementRepository;

    @EJB
    private CompteCourantRepository compteCourantRepository;

    @EJB
    private TypeOperationRepository typeOperationRepository;

    @EJB
    private AuthorizationService authorizationService;

    private static final String TABLE_MOUVEMENT = "mouvement";

    /**
     * Crée un mouvement (INSERT) avec vérification des permissions
     */
    public MouvementDTO create(MouvementDTO mouvementDTO, UserSessionBeanRemote userSession) {
        // Vérifier la permission INSERT
        if (!authorizationService.checkPermission(userSession, TABLE_MOUVEMENT, "INSERT")) {
            throw new SecurityException("Vous n'avez pas la permission d'insérer un mouvement");
        }

        CompteCourant compte = compteCourantRepository.findById(mouvementDTO.getIdCompte())
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        TypeOperation typeOperation = typeOperationRepository.findById(mouvementDTO.getIdTypeOperation())
                .orElseThrow(() -> new RuntimeException("Type d'opération non trouvé"));

        Mouvement mouvement = Mouvement.builder()
                .compte(compte)
                .typeOperation(typeOperation)
                .montant(mouvementDTO.getMontant())
                .soldeAvantOperation(mouvementDTO.getSoldeAvantOperation())
                .soldeApresOperation(mouvementDTO.getSoldeApresOperation())
                .dateOperation(LocalDateTime.now())
                .reference(mouvementDTO.getReference())
                .libelleOperation(mouvementDTO.getLibelleOperation())
                .idAdministrateur(userSession.getUtilisateur().getIdUtilisateur())
                .build();

        mouvement = mouvementRepository.save(mouvement);
        return toDTO(mouvement);
    }

    /**
     * Récupère un mouvement par ID (SELECT) avec vérification des permissions
     */
    public Optional<MouvementDTO> findById(Long id, UserSessionBeanRemote userSession) {
        // Vérifier la permission SELECT
        if (!authorizationService.checkPermission(userSession, TABLE_MOUVEMENT, "SELECT")) {
            throw new SecurityException("Vous n'avez pas la permission de consulter les mouvements");
        }

        return mouvementRepository.findById(id)
                .map(this::toDTO);
    }

    /**
     * Récupère tous les mouvements (SELECT) avec vérification des permissions
     */
    public List<MouvementDTO> findAll(UserSessionBeanRemote userSession) {
        // Vérifier la permission SELECT
        if (!authorizationService.checkPermission(userSession, TABLE_MOUVEMENT, "SELECT")) {
            throw new SecurityException("Vous n'avez pas la permission de consulter les mouvements");
        }

        return mouvementRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour un mouvement (UPDATE) avec vérification des permissions
     */
    public MouvementDTO update(MouvementDTO mouvementDTO, UserSessionBeanRemote userSession) {
        // Vérifier la permission UPDATE
        if (!authorizationService.checkPermission(userSession, TABLE_MOUVEMENT, "UPDATE")) {
            throw new SecurityException("Vous n'avez pas la permission de modifier un mouvement");
        }

        Mouvement mouvement = mouvementRepository.findById(mouvementDTO.getIdMouvement())
                .orElseThrow(() -> new RuntimeException("Mouvement non trouvé"));

        if (mouvementDTO.getIdCompte() != null) {
            CompteCourant compte = compteCourantRepository.findById(mouvementDTO.getIdCompte())
                    .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
            mouvement.setCompte(compte);
        }

        if (mouvementDTO.getIdTypeOperation() != null) {
            TypeOperation typeOperation = typeOperationRepository.findById(mouvementDTO.getIdTypeOperation())
                    .orElseThrow(() -> new RuntimeException("Type d'opération non trouvé"));
            mouvement.setTypeOperation(typeOperation);
        }

        if (mouvementDTO.getMontant() != null) {
            mouvement.setMontant(mouvementDTO.getMontant());
        }
        if (mouvementDTO.getLibelleOperation() != null) {
            mouvement.setLibelleOperation(mouvementDTO.getLibelleOperation());
        }

        mouvement = mouvementRepository.save(mouvement);
        return toDTO(mouvement);
    }

    /**
     * Supprime un mouvement (DELETE) avec vérification des permissions
     */
    public void delete(Long id, UserSessionBeanRemote userSession) {
        // Vérifier la permission DELETE
        if (!authorizationService.checkPermission(userSession, TABLE_MOUVEMENT, "DELETE")) {
            throw new SecurityException("Vous n'avez pas la permission de supprimer un mouvement");
        }

        mouvementRepository.deleteById(id);
    }

    private MouvementDTO toDTO(Mouvement mouvement) {
        return MouvementDTO.builder()
                .idMouvement(mouvement.getIdMouvement())
                .idCompte(mouvement.getCompte().getIdCompte())
                .numeroCompte(mouvement.getCompte().getNumeroCompte())
                .idTypeOperation(mouvement.getTypeOperation().getIdTypeOperation())
                .montant(mouvement.getMontant())
                .soldeAvantOperation(mouvement.getSoldeAvantOperation())
                .soldeApresOperation(mouvement.getSoldeApresOperation())
                .dateOperation(mouvement.getDateOperation())
                .reference(mouvement.getReference())
                .libelleOperation(mouvement.getLibelleOperation())
                .idAdministrateur(mouvement.getIdAdministrateur())
                .build();
    }
}
