package com.banque.situationbancaire.ejb.impl;

import com.banque.situationbancaire.dto.CompteCourantDTO;
import com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote;
import com.banque.situationbancaire.entity.*;
import com.banque.situationbancaire.entity.enums.StatutCompte;
import com.banque.situationbancaire.mapper.CompteCourantMapper;
import com.banque.situationbancaire.repository.*;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.logging.Logger;

/**
 * Implémentation complète du service de gestion des comptes courants avec DTOs
 */
@Stateless
public class CompteCourantServiceImpl implements CompteCourantServiceRemote {

    private static final Logger LOGGER = Logger.getLogger(CompteCourantServiceImpl.class.getName());

    @Inject
    private CompteCourantRepository compteCourantRepository;
    
    @Inject
    private ClientRepository clientRepository;
    
    @Inject
    private MouvementRepository mouvementRepository;

    @Override
    public CompteCourantDTO creerCompte(CompteCourantDTO compteDTO, Long idClient) {
        LOGGER.info("Création d'un nouveau compte pour le client : " + idClient);
        
        // Vérifier que le client existe
        Optional<Client> clientOpt = clientRepository.findById(idClient);
        if (clientOpt.isEmpty()) {
            throw new IllegalArgumentException("Client non trouvé");
        }
        
        Client client = clientOpt.get();
        
        // Créer un type de compte par défaut temporaire
        TypeCompte typeCompteDefaut = TypeCompte.builder()
            .codeType("STANDARD")
            .libelle("Compte Standard")
            .description("Compte courant standard")
            .build();
        
        // Créer l'entité CompteCourant
        CompteCourant compte = CompteCourant.builder()
            .client(client)
            .typeCompte(typeCompteDefaut)
            .numeroCompte(genererNumeroCompte())
            .libelleCompte(compteDTO.getLibelleCompte() != null ? compteDTO.getLibelleCompte() : "Compte courant")
            .devise("XOF")
            .statut(StatutCompte.OUVERT)
            .dateOuverture(LocalDate.now())
            .soldeInitial(compteDTO.getSoldeInitial() != null ? compteDTO.getSoldeInitial() : BigDecimal.ZERO)
            .build();
        
        CompteCourant compteCree = compteCourantRepository.save(compte);
        
        return CompteCourantMapper.toDTO(compteCree);
    }

    @Override
    public CompteCourantDTO rechercherCompteParId(Long idCompte) {
        LOGGER.info("Recherche du compte par ID : " + idCompte);
        Optional<CompteCourant> compte = compteCourantRepository.findById(idCompte);
        return compte.map(CompteCourantMapper::toDTO).orElse(null);
    }

    @Override
    public CompteCourantDTO rechercherCompteParNumero(String numeroCompte) {
        LOGGER.info("Recherche du compte par numéro : " + numeroCompte);
        Optional<CompteCourant> compte = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compte.isPresent()) {
            CompteCourantDTO dto = CompteCourantMapper.toDTO(compte.get());
            // Calculer le solde actuel
            dto.setSolde(calculerSoldeActuel(numeroCompte));
            return dto;
        }
        return null;
    }

    @Override
    public List<CompteCourantDTO> listerComptesParClient(Long idClient) {
        LOGGER.info("Listing des comptes pour le client : " + idClient);
        List<CompteCourant> comptes = compteCourantRepository.findByClientId(idClient);
        return comptes.stream()
                .map(compte -> {
                    CompteCourantDTO dto = CompteCourantMapper.toDTO(compte);
                    dto.setSolde(calculerSoldeActuel(compte.getNumeroCompte()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculerSoldeActuel(String numeroCompte) {
        LOGGER.info("Calcul du solde pour le compte : " + numeroCompte);
        
        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé : " + numeroCompte);
        }
        
        CompteCourant compte = compteOpt.get();
        
        // Solde = solde initial + somme de tous les mouvements
        BigDecimal soldeInitial = compte.getSoldeInitial();
        BigDecimal totalMouvements = mouvementRepository.calculerSoldeMouvements(compte.getIdCompte());
        
        return soldeInitial.add(totalMouvements != null ? totalMouvements : BigDecimal.ZERO);
    }

    @Override
    public CompteCourantDTO obtenirInfosCompte(String numeroCompte) {
        LOGGER.info("Obtention des infos pour le compte : " + numeroCompte);
        
        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            return null;
        }
        
        CompteCourant compte = compteOpt.get();
        CompteCourantDTO dto = CompteCourantMapper.toDTO(compte);
        
        // Enrichir avec le solde actuel et découvert autorisé
        dto.setSolde(calculerSoldeActuel(numeroCompte));
        if (compte.getTypeCompte() != null && compte.getTypeCompte().getParametreActuel() != null) {
            dto.setDecouvertAutorise(compte.getTypeCompte().getParametreActuel().getMontantDecouvertAutorise());
        }
        
        return dto;
    }

    @Override
    public void fermerCompte(String numeroCompte, String motif) {
        LOGGER.info("Fermeture du compte : " + numeroCompte + " - Motif : " + motif);
        
        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé : " + numeroCompte);
        }
        
        CompteCourant compte = compteOpt.get();
        
        // Vérifier que le solde est proche de zéro (≤ 1 XOF)
        BigDecimal solde = calculerSoldeActuel(numeroCompte);
        if (solde.abs().compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalStateException("Impossible de fermer un compte avec un solde non nul : " + solde);
        }
        
        compte.setStatut(StatutCompte.FERME);
        compte.setDateFermeture(LocalDate.now());
        compte.setMotifFermeture(motif);
        
        compteCourantRepository.update(compte);
    }

    @Override
    public void bloquerCompte(String numeroCompte) {
        LOGGER.info("Blocage du compte : " + numeroCompte);
        
        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé : " + numeroCompte);
        }
        
        CompteCourant compte = compteOpt.get();
        compte.setStatut(StatutCompte.BLOQUE);
        
        compteCourantRepository.update(compte);
    }

    @Override
    public void debloquerCompte(String numeroCompte) {
        LOGGER.info("Déblocage du compte : " + numeroCompte);
        
        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé : " + numeroCompte);
        }
        
        CompteCourant compte = compteOpt.get();
        compte.setStatut(StatutCompte.OUVERT);
        
        compteCourantRepository.update(compte);
    }

    @Override
    public boolean compteExisteEtActif(String numeroCompte) {
        LOGGER.info("Vérification de l'existence et du statut du compte : " + numeroCompte);
        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        return compteOpt.isPresent() && compteOpt.get().getStatut() == StatutCompte.OUVERT;
    }

    @Override
    public List<String> listerTypesComptesDisponibles() {
        LOGGER.info("Récupération des types de comptes disponibles");
        
        // Pour l'instant, retourner une liste statique des types de comptes
        // En production, cela viendrait de typeCompteRepository.findAll()
        return java.util.Arrays.asList(
            "STANDARD - Compte courant standard avec fonctionnalités de base",
            "PREMIUM - Compte avec avantages et plafonds élevés", 
            "ETUDIANT - Compte spécialement conçu pour les étudiants",
            "BUSINESS - Compte professionnel pour les entreprises"
        );
    }

    /**
     * Génère un numéro de compte unique
     */
    private String genererNumeroCompte() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 999);
        return String.format("CC%d%03d", timestamp, random);
    }
}