package com.banque.situationbancaire.dto;

import lombok.*;
import java.io.Serializable;

/**
 * DTO pour la Direction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectionDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer idDirection;
    private Integer niveau;
}
