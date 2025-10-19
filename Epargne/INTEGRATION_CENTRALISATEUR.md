# Intégration Module Épargne - Centralisateur Java

## Architecture de Communication

Le module Épargne (.NET) communique avec le Centralisateur (Java) via **Web Services REST** (contrairement aux modules Prêt et Situation Bancaire qui utilisent EJB).

```
┌─────────────────┐              ┌──────────────────┐
│                 │   HTTP REST  │                  │
│  Centralisateur │◄────────────►│  Module Épargne  │
│     (Java)      │              │     (.NET)       │
│                 │              │                  │
└─────────────────┘              └──────────────────┘
        │                                 │
        ▼                                 ▼
   PostgreSQL                        PostgreSQL
  (situation_bancaire)              (compte_epargne_db)
```

## Configuration du Centralisateur

### 1. Fichier `application.properties`

Ajouter la configuration suivante dans le fichier `application.properties` du Centralisateur :

```properties
# Configuration du module Épargne
epargne.api.url=http://localhost:5000/api
epargne.api.timeout=30000
```

### 2. Créer un Client REST

Créer une classe `EpargneRestClient` dans le package `com.banque.centralisateur.client` :

```java
package com.banque.centralisateur.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class EpargneRestClient {
    
    @Value("${epargne.api.url}")
    private String epargneApiUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public EpargneRestClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    private String getUrl(String endpoint) {
        return epargneApiUrl + endpoint;
    }
    
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
    
    // Méthode générique pour GET
    public <T> T get(String endpoint, Class<T> responseType) {
        return restTemplate.getForObject(getUrl(endpoint), responseType);
    }
    
    // Méthode générique pour POST
    public <T, R> R post(String endpoint, T body, Class<R> responseType) {
        HttpEntity<T> request = new HttpEntity<>(body, getHeaders());
        return restTemplate.postForObject(getUrl(endpoint), request, responseType);
    }
    
    // Méthode générique pour PUT
    public <T, R> R put(String endpoint, T body, Class<R> responseType) {
        HttpEntity<T> request = new HttpEntity<>(body, getHeaders());
        restTemplate.put(getUrl(endpoint), request);
        // Pour PUT, récupérer la ressource mise à jour
        return get(endpoint, responseType);
    }
}
```

### 3. Créer les DTOs côté Java

Créer les DTOs dans le package `com.banque.centralisateur.dto.epargne` :

#### ClientEpargneDTO.java
```java
package com.banque.centralisateur.dto.epargne;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ClientEpargneDTO {
    @JsonProperty("IdClient")
    private Long idClient;
    
    @JsonProperty("NumeroClient")
    private String numeroClient;
    
    @JsonProperty("Nom")
    private String nom;
    
    @JsonProperty("Prenom")
    private String prenom;
    
    @JsonProperty("DateNaissance")
    private LocalDate dateNaissance;
    
    @JsonProperty("NumCin")
    private String numCin;
    
    @JsonProperty("Email")
    private String email;
    
    @JsonProperty("MotDePasse")
    private String motDePasse;
    
    // ... autres champs et getters/setters
}
```

#### CompteEpargneDTO.java
```java
package com.banque.centralisateur.dto.epargne;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CompteEpargneDTO {
    @JsonProperty("IdCompte")
    private Integer idCompte;
    
    @JsonProperty("NumeroCompte")
    private String numeroCompte;
    
    @JsonProperty("Solde")
    private BigDecimal solde;
    
    @JsonProperty("SoldeDisponible")
    private BigDecimal soldeDisponible;
    
    @JsonProperty("TypeCompte")
    private TypeCompteEpargneDTO typeCompte;
    
    // ... autres champs et getters/setters
}
```

### 4. Créer un Service Épargne

Créer `EpargneService` dans le package `com.banque.centralisateur.service` :

```java
package com.banque.centralisateur.service;

import com.banque.centralisateur.client.EpargneRestClient;
import com.banque.centralisateur.dto.epargne.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EpargneService {
    
    @Autowired
    private EpargneRestClient epargneClient;
    
    // Clients
    public ClientEpargneDTO creerClient(ClientEpargneDTO client) {
        return epargneClient.post("/clients", client, ClientEpargneDTO.class);
    }
    
    public ClientEpargneDTO authentifierClient(LoginDTO login) {
        return epargneClient.post("/clients/login", login, ClientEpargneDTO.class);
    }
    
    public ClientEpargneDTO getClientById(Long id) {
        return epargneClient.get("/clients/" + id, ClientEpargneDTO.class);
    }
    
    // Types de comptes
    public List<TypeCompteEpargneDTO> getTypesComptesActifs() {
        return epargneClient.get("/typescomptes/actifs", List.class);
    }
    
    // Comptes épargne
    public List<CompteEpargneDTO> getComptesByClientId(Long clientId) {
        return epargneClient.get("/comptesepargne/client/" + clientId, List.class);
    }
    
    public CompteEpargneDTO creerCompteEpargne(CreationCompteEpargneDTO creation) {
        return epargneClient.post("/comptesepargne", creation, CompteEpargneDTO.class);
    }
    
    public OperationEpargneDTO effectuerDepot(Integer compteId, DepotDTO depot) {
        return epargneClient.post("/comptesepargne/" + compteId + "/depot", 
                                  depot, OperationEpargneDTO.class);
    }
    
    public OperationEpargneDTO effectuerRetrait(Integer compteId, RetraitDTO retrait) {
        return epargneClient.post("/comptesepargne/" + compteId + "/retrait", 
                                  retrait, OperationEpargneDTO.class);
    }
    
    public List<OperationEpargneDTO> getHistoriqueOperations(Integer compteId) {
        return epargneClient.get("/comptesepargne/" + compteId + "/operations", List.class);
    }
}
```

### 5. Créer un Servlet pour l'interface utilisateur

Créer `EpargneServlet` dans le package `com.banque.centralisateur.servlet` :

```java
package com.banque.centralisateur.servlet;

import com.banque.centralisateur.service.EpargneService;
import com.banque.centralisateur.dto.epargne.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/epargne/*")
public class EpargneServlet extends HttpServlet {
    
    @Inject
    private EpargneService epargneService;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getPathInfo();
        
        if (action == null || action.equals("/")) {
            action = "/liste-comptes";
        }
        
        try {
            switch (action) {
                case "/liste-comptes":
                    afficherListeComptes(request, response);
                    break;
                case "/nouveau-compte":
                    afficherFormulaireNouveauCompte(request, response);
                    break;
                case "/depot":
                    afficherFormulaireDepot(request, response);
                    break;
                case "/retrait":
                    afficherFormulaireRetrait(request, response);
                    break;
                case "/historique":
                    afficherHistorique(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    private void afficherListeComptes(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Long clientId = (Long) session.getAttribute("clientId");
        
        if (clientId != null) {
            var comptes = epargneService.getComptesByClientId(clientId);
            request.setAttribute("comptes", comptes);
        }
        
        request.getRequestDispatcher("/WEB-INF/templates/epargne/liste-comptes.html")
               .forward(request, response);
    }
    
    // ... autres méthodes
}
```

## Endpoints API disponibles

### Clients
- `POST /api/clients` - Créer un client
- `POST /api/clients/login` - Authentifier un client
- `GET /api/clients/{id}` - Récupérer un client
- `PUT /api/clients/{id}` - Mettre à jour un client

### Types de Comptes
- `GET /api/typescomptes/actifs` - Liste des types actifs

### Comptes Épargne
- `GET /api/comptesepargne/client/{clientId}` - Comptes d'un client
- `POST /api/comptesepargne` - Créer un compte
- `POST /api/comptesepargne/{id}/depot` - Effectuer un dépôt
- `POST /api/comptesepargne/{id}/retrait` - Effectuer un retrait
- `GET /api/comptesepargne/{id}/operations` - Historique

## Gestion des erreurs

Le module Épargne renvoie des codes HTTP standard :

- **200 OK** : Succès
- **201 Created** : Ressource créée
- **400 Bad Request** : Validation échouée (avec message d'erreur)
- **404 Not Found** : Ressource non trouvée
- **500 Internal Server Error** : Erreur serveur

Format des erreurs :
```json
{
  "message": "Description de l'erreur"
}
```

## Testing de l'intégration

### 1. Démarrer les modules

```bash
# Terminal 1 : Module Épargne
cd Epargne
dotnet run

# Terminal 2 : Centralisateur
cd Centralisateur
wildfly:run
```

### 2. Tester la communication

```bash
# Test direct de l'API Épargne
curl http://localhost:5000/api/typescomptes/actifs

# Via le Centralisateur
curl http://localhost:8080/centralisateur/epargne/types-comptes
```

## Dépannage

### Le Centralisateur ne peut pas joindre le module Épargne

1. Vérifier que le module Épargne est démarré :
   ```bash
   curl http://localhost:5000/api/typescomptes/actifs
   ```

2. Vérifier la configuration CORS dans le module Épargne (déjà configuré)

3. Vérifier les logs du Centralisateur pour voir les erreurs de connexion

### Problèmes de sérialisation JSON

Les DTOs côté Java doivent correspondre exactement aux DTOs .NET. Utiliser les annotations `@JsonProperty` avec les bons noms de propriétés (PascalCase côté .NET).

### Timeout

Augmenter le timeout dans `application.properties` :
```properties
epargne.api.timeout=60000
```

## Sécurité

- Les mots de passe sont hachés avec SHA-256 (compatible entre Java et .NET)
- CORS est configuré pour accepter les requêtes du Centralisateur
- HTTPS devrait être utilisé en production

## Performance

- Utiliser un cache pour les types de comptes (rarement modifiés)
- Pagination pour l'historique des opérations
- Connection pooling pour les appels REST
