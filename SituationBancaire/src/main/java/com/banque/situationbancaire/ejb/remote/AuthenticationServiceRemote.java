package com.banque.situationbancaire.ejb.remote;

import com.banque.situationbancaire.dto.LoginRequestDTO;
import com.banque.situationbancaire.dto.LoginResponseDTO;

import jakarta.ejb.Remote;

/**
 * Interface Remote pour le service d'authentification
 */
@Remote
public interface AuthenticationServiceRemote {
    
    LoginResponseDTO authenticate(LoginRequestDTO loginRequest, UserSessionBeanRemote userSession);
    
    void logout(UserSessionBeanRemote userSession);
}
