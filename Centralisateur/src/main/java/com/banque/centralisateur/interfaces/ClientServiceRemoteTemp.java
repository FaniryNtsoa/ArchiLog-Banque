package com.banque.centralisateur.interfaces;

// Interfaces temporaires pour éviter les dépendances manquantes
// En production, ces interfaces viendront du JAR client de SituationBancaire

import java.util.List;
import java.math.BigDecimal;

/**
 * Interface Remote temporaire pour ClientService
 */
public interface ClientServiceRemoteTemp {
    Object rechercherClientParId(Long idClient);
    Object rechercherClientParNumero(String numeroClient);
    List<Object> listerTousLesClients();
    Object rechercherClientParEmail(String email);
    boolean existeParEmail(String email);
    boolean existeParNumCin(String numCin);
}

/**
 * Interface Remote temporaire pour CompteCourantService
 */
interface CompteCourantServiceRemoteTemp {
    Object rechercherCompteParId(Long idCompte);
    Object rechercherCompteParNumero(String numeroCompte);
    List<Object> listerComptesParClient(Long idClient);
    BigDecimal calculerSoldeActuel(String numeroCompte);
}