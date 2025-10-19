package com.banque.pret.ejb.impl;

import com.banque.pret.dto.TypePretDTO;
import com.banque.pret.ejb.remote.TypePretServiceRemote;
import com.banque.pret.entity.TypePret;
import com.banque.pret.mapper.TypePretMapper;
import com.banque.pret.repository.TypePretRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implémentation du service type de prêt
 */
@Stateless
public class TypePretServiceImpl implements TypePretServiceRemote {

    private static final Logger LOGGER = Logger.getLogger(TypePretServiceImpl.class.getName());

    @Inject
    private TypePretRepository typePretRepository;

    @Override
    public TypePretDTO creerTypePret(TypePretDTO typePretDTO) {
        LOGGER.info("Création d'un nouveau type de prêt : " + typePretDTO.getLibelle());
        
        // Vérifier que le code n'existe pas déjà
        Optional<TypePret> existant = typePretRepository.findByCodeType(typePretDTO.getCodeType());
        if (existant.isPresent()) {
            throw new IllegalArgumentException("Un type de prêt avec ce code existe déjà");
        }
        
        // Conversion DTO vers entité
        TypePret typePret = TypePretMapper.toEntity(typePretDTO);
        
        // Sauvegarder
        TypePret typePretCree = typePretRepository.save(typePret);
        
        LOGGER.info("Type de prêt créé avec succès : " + typePretCree.getCodeType());
        
        return TypePretMapper.toDTO(typePretCree);
    }

    @Override
    public TypePretDTO rechercherTypePretParId(Long idTypePret) {
        LOGGER.info("Recherche du type de prêt par ID : " + idTypePret);
        Optional<TypePret> typePret = typePretRepository.findById(idTypePret);
        return typePret.map(TypePretMapper::toDTO).orElse(null);
    }

    @Override
    public TypePretDTO rechercherTypePretParCode(String codeType) {
        LOGGER.info("Recherche du type de prêt par code : " + codeType);
        Optional<TypePret> typePret = typePretRepository.findByCodeType(codeType);
        return typePret.map(TypePretMapper::toDTO).orElse(null);
    }

    @Override
    public TypePretDTO modifierTypePret(TypePretDTO typePretDTO) {
        LOGGER.info("Modification du type de prêt : " + typePretDTO.getIdTypePret());
        
        Optional<TypePret> existantOpt = typePretRepository.findById(typePretDTO.getIdTypePret());
        if (existantOpt.isEmpty()) {
            throw new IllegalArgumentException("Type de prêt non trouvé");
        }
        
        TypePret existant = existantOpt.get();
        
        // Vérifier que le code n'est pas utilisé par un autre type
        if (!existant.getCodeType().equals(typePretDTO.getCodeType())) {
            Optional<TypePret> autreType = typePretRepository.findByCodeType(typePretDTO.getCodeType());
            if (autreType.isPresent()) {
                throw new IllegalArgumentException("Un autre type de prêt utilise déjà ce code");
            }
        }
        
        // Mettre à jour
        TypePretMapper.updateEntity(existant, typePretDTO);
        TypePret typePretModifie = typePretRepository.update(existant);
        
        return TypePretMapper.toDTO(typePretModifie);
    }

    @Override
    public List<TypePretDTO> listerTousLesTypesPrets() {
        LOGGER.info("Récupération de tous les types de prêts");
        List<TypePret> typesPrets = typePretRepository.findAll();
        return typesPrets.stream()
                .map(TypePretMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TypePretDTO> listerTypesPretsActifs() {
        LOGGER.info("Récupération des types de prêts actifs");
        List<TypePret> typesPrets = typePretRepository.findAllActifs();
        return typesPrets.stream()
                .map(TypePretMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void supprimerTypePret(Long idTypePret) {
        LOGGER.info("Suppression du type de prêt : " + idTypePret);
        
        Optional<TypePret> typePretOpt = typePretRepository.findById(idTypePret);
        if (typePretOpt.isEmpty()) {
            throw new IllegalArgumentException("Type de prêt non trouvé");
        }
        
        TypePret typePret = typePretOpt.get();
        
        // Vérifier qu'il n'y a pas de prêts associés
        if (!typePret.getPrets().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer un type de prêt ayant des prêts associés");
        }
        
        typePretRepository.deleteById(idTypePret);
    }
}
