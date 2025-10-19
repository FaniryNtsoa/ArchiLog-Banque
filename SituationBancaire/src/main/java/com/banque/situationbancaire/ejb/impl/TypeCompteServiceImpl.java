package com.banque.situationbancaire.ejb.impl;

import com.banque.situationbancaire.dto.TypeCompteDTO;
import com.banque.situationbancaire.ejb.remote.TypeCompteServiceRemote;
import com.banque.situationbancaire.entity.TypeCompte;
import com.banque.situationbancaire.mapper.TypeCompteMapper;
import com.banque.situationbancaire.repository.TypeCompteRepository;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.logging.Logger;

/**
 * Implémentation du service de gestion des types de compte
 */
@Stateless
public class TypeCompteServiceImpl implements TypeCompteServiceRemote {
    
    private static final Logger LOGGER = Logger.getLogger(TypeCompteServiceImpl.class.getName());
    
    @Inject
    private TypeCompteRepository typeCompteRepository;
    
    @Override
    public List<TypeCompteDTO> listerTousLesTypesCompte() {
        try {
            LOGGER.info("Récupération de tous les types de compte");
            List<TypeCompte> typesCompte = typeCompteRepository.findAll();
            return typesCompte.stream()
                    .map(TypeCompteMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la récupération des types de compte: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des types de compte", e);
        }
    }
    
    @Override
    public TypeCompteDTO rechercherTypeCompteParId(Long idTypeCompte) {
        if (idTypeCompte == null) {
            throw new IllegalArgumentException("L'ID du type de compte ne peut pas être null");
        }
        
        try {
            LOGGER.info("Recherche du type de compte avec l'ID: " + idTypeCompte);
            Optional<TypeCompte> typeCompte = typeCompteRepository.findById(idTypeCompte);
            return typeCompte.map(TypeCompteMapper::toDTO).orElse(null);
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la recherche du type de compte: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche du type de compte", e);
        }
    }
    
    @Override
    public TypeCompteDTO rechercherTypeCompteParCode(String codeType) {
        if (codeType == null || codeType.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code du type de compte ne peut pas être vide");
        }
        
        try {
            LOGGER.info("Recherche du type de compte avec le code: " + codeType);
            Optional<TypeCompte> typeCompte = typeCompteRepository.findByCode(codeType.trim().toUpperCase());
            return typeCompte.map(TypeCompteMapper::toDTO).orElse(null);
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la recherche du type de compte: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche du type de compte", e);
        }
    }
    
    @Override
    public boolean existeParCode(String codeType) {
        if (codeType == null || codeType.trim().isEmpty()) {
            return false;
        }
        
        try {
            return typeCompteRepository.existsByCode(codeType.trim().toUpperCase());
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la vérification de l'existence du type de compte: " + e.getMessage());
            return false;
        }
    }
}