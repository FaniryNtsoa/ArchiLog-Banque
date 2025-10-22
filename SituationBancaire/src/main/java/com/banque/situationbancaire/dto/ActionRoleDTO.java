package com.banque.situationbancaire.dto;

import lombok.*;
import java.io.Serializable;

/**
 * DTO pour ActionRole
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionRoleDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer idActionRole;
    private String nomTable;
    private String actionAutorisee;
    private Integer roleRequis;
}
