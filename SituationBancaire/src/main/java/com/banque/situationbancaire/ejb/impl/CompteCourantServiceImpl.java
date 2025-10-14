package com.banque.situationbancaire.ejb.impl;

import com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote;
import com.banque.situationbancaire.entity.Client;
import com.banque.situationbancaire.entity.CompteCourant;
import com.banque.situationbancaire.entity.Mouvement;
import com.banque.situationbancaire.entity.TypeCompte;
import com.banque.situationbancaire.entity.enums.StatutCompte;
import com.banque.situationbancaire.repository.ClientRepository;
import com.banque.situationbancaire.repository.CompteCourantRepository;
import com.banque.situationbancaire.repository.TypeCompteRepository;
import com.banque.situationbancaire.repository.MouvementRepository;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implémentation du service de gestion des comptes courants
 */
@Stateless
@Transactional
public class CompteCourantServiceImpl implements CompteCourantServiceRemote {

    private static final Logger LOGGER = Logger.getLogger(CompteCourantServiceImpl.class.getName());

    @EJB
    private CompteCourantRepository compteCourantRepository;

    @EJB
    private ClientRepository clientRepository;

    @EJB
    private TypeCompteRepository typeCompteRepository;

    @EJB
    private MouvementRepository mouvementRepository;

    @Override
    public CompteCourant creerCompte(CompteCourant compte, Long idClient) {
        LOGGER.info("Création d'un nouveau compte pour le client : " + idClient);
        
        // Vérifier que le client existe
        Optional<Client> clientOpt = clientRepository.findById(idClient);
        if (clientOpt.isEmpty()) {
            throw new IllegalArgumentException("Client non trouvé");
        }
        
        Client client = clientOpt.get();
        
        // Générer un numéro de compte unique
        compte.setNumeroCompte(genererNumeroCompte());
        compte.setClient(client);
        compte.setDateOuverture(LocalDate.now());
        compte.setStatut(StatutCompte.OUVERT);
        
        // Si aucun type de compte spécifié, utiliser le type par défaut
        if (compte.getTypeCompte() == null) {
            Optional<TypeCompte> typeCompteOpt = typeCompteRepository.findByDefaut();
            if (typeCompteOpt.isPresent()) {
                compte.setTypeCompte(typeCompteOpt.get());
            }
        }
        
        return compteCourantRepository.save(compte);
    }

    @Override
    public CompteCourant rechercherCompteParId(Long idCompte) {
        LOGGER.info("Recherche du compte par ID : " + idCompte);
        Optional<CompteCourant> compte = compteCourantRepository.findById(idCompte);
        return compte.orElse(null);
    }

    @Override
    public CompteCourant rechercherCompteParNumero(String numeroCompte) {
        LOGGER.info("Recherche du compte par numéro : " + numeroCompte);
        Optional<CompteCourant> compte = compteCourantRepository.findByNumeroCompte(numeroCompte);
        return compte.orElse(null);
    }

    @Override
    public List<CompteCourant> listerComptesParClient(Long idClient) {
        LOGGER.info("Récupération des comptes du client : " + idClient);
        return compteCourantRepository.findByClientId(idClient);
    }

    @Override
    public BigDecimal calculerSoldeActuel(String numeroCompte) {
        LOGGER.info("Calcul du solde actuel pour le compte : " + numeroCompte);
        
        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé");
        }
        
        CompteCourant compte = compteOpt.get();
        
        // Calculer le solde : solde initial du client + somme de tous les mouvements
        BigDecimal soldeInitial = compte.getClient().getSoldeInitial();
        BigDecimal totalMouvements = mouvementRepository.calculerSoldeMouvements(compte.getIdCompte());
        
        return soldeInitial.add(totalMouvements != null ? totalMouvements : BigDecimal.ZERO);
    }

    @Override
    public CompteCourant obtenirInfosCompte(String numeroCompte) {
        LOGGER.info("Obtention des informations du compte : " + numeroCompte);
        
        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            return null;
        }
        
        CompteCourant compte = compteOpt.get();
        // Note: Le solde peut être calculé côté client si nécessaire
        return compte;
    }

    @Override
    public void fermerCompte(String numeroCompte, String motif) {
        LOGGER.info("Fermeture du compte : " + numeroCompte);
        
        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé");
        }
        
        CompteCourant compte = compteOpt.get();
        
        // Vérifier que le solde est à zéro ou proche de zéro
        BigDecimal solde = calculerSoldeActuel(numeroCompte);
        if (solde.abs().compareTo(new BigDecimal("0.01")) > 0) {
            throw new IllegalStateException("Impossible de fermer un compte avec un solde non nul");
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
            throw new IllegalArgumentException("Compte non trouvé");
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
            throw new IllegalArgumentException("Compte non trouvé");
        }
        
        CompteCourant compte = compteOpt.get();
        compte.setStatut(StatutCompte.OUVERT);
        
        compteCourantRepository.update(compte);
    }

    @Override
    public boolean compteExisteEtActif(String numeroCompte) {
        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        return compteOpt.isPresent() && compteOpt.get().getStatut() == StatutCompte.OUVERT;
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