package com.banque.situationbancaire.service;

import com.banque.situationbancaire.dto.DirectionDTO;
import com.banque.situationbancaire.entity.Direction;
import com.banque.situationbancaire.repository.DirectionRepository;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service de gestion des directions
 */
@Stateless
public class DirectionService {

    @EJB
    private DirectionRepository directionRepository;

    public DirectionDTO create(DirectionDTO directionDTO) {
        Direction direction = Direction.builder()
                .niveau(directionDTO.getNiveau())
                .build();
        
        direction = directionRepository.save(direction);
        return toDTO(direction);
    }

    public Optional<DirectionDTO> findById(Integer id) {
        return directionRepository.findById(id)
                .map(this::toDTO);
    }

    public List<DirectionDTO> findAll() {
        return directionRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DirectionDTO update(DirectionDTO directionDTO) {
        Direction direction = directionRepository.findById(directionDTO.getIdDirection())
                .orElseThrow(() -> new RuntimeException("Direction non trouvée"));
        
        direction.setNiveau(directionDTO.getNiveau());
        direction = directionRepository.save(direction);
        return toDTO(direction);
    }

    public void delete(Integer id) {
        directionRepository.deleteById(id);
    }

    private DirectionDTO toDTO(Direction direction) {
        return DirectionDTO.builder()
                .idDirection(direction.getIdDirection())
                .niveau(direction.getNiveau())
                .build();
    }
}
