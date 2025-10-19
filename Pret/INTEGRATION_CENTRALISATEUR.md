# Guide d'Intégration du Module Prêt au Centralisateur

## 1. Prérequis

### Configuration WildFly
1. Déployer le module Prêt (`pret.war`) sur WildFly
2. Créer la base de données PostgreSQL `pret_db`
3. Exécuter le script `script_pret.sql` pour créer les tables
4. Exécuter le script `data_init_pret.sql` pour les données de test

## 2. Dépendances Maven dans le Centralisateur

Ajouter au `pom.xml` du Centralisateur :

```xml
<!-- Client EJB du module Prêt -->
<dependency>
    <groupId>com.banque</groupId>
    <artifactId>pret</artifactId>
    <version>1.0.0</version>
    <classifier>client</classifier>
    <scope>provided</scope>
</dependency>
```

## 3. Configuration EJB Client

### Mise à jour de `jboss-ejb-client.properties`

```properties
# Connexion au module Prêt
remote.connection.pret.host=localhost
remote.connection.pret.port=8080
remote.connection.pret.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS=false
```

### EJBClientFactory - Ajout des méthodes pour le module Prêt

```java
// Dans com.banque.centralisateur.ejb.EJBClientFactory

public static PretServiceRemote getPretService() {
    try {
        final String appName = "";
        final String moduleName = "pret";
        final String distinctName = "";
        final String beanName = PretServiceImpl.class.getSimpleName();
        final String viewClassName = PretServiceRemote.class.getName();
        
        String jndiName = "ejb:" + appName + "/" + moduleName + "/" + 
                         distinctName + "/" + beanName + "!" + viewClassName;
        
        return (PretServiceRemote) context.lookup(jndiName);
    } catch (NamingException e) {
        throw new RuntimeException("Erreur lors de la recherche EJB PretService", e);
    }
}

public static EcheanceServiceRemote getEcheanceService() {
    try {
        final String appName = "";
        final String moduleName = "pret";
        final String distinctName = "";
        final String beanName = EcheanceServiceImpl.class.getSimpleName();
        final String viewClassName = EcheanceServiceRemote.class.getName();
        
        String jndiName = "ejb:" + appName + "/" + moduleName + "/" + 
                         distinctName + "/" + beanName + "!" + viewClassName;
        
        return (EcheanceServiceRemote) context.lookup(jndiName);
    } catch (NamingException e) {
        throw new RuntimeException("Erreur lors de la recherche EJB EcheanceService", e);
    }
}

public static TypePretServiceRemote getTypePretService() {
    try {
        final String appName = "";
        final String moduleName = "pret";
        final String distinctName = "";
        final String beanName = TypePretServiceImpl.class.getSimpleName();
        final String viewClassName = TypePretServiceRemote.class.getName();
        
        String jndiName = "ejb:" + appName + "/" + moduleName + "/" + 
                         distinctName + "/" + beanName + "!" + viewClassName;
        
        return (TypePretServiceRemote) context.lookup(jndiName);
    } catch (NamingException e) {
        throw new RuntimeException("Erreur lors de la recherche EJB TypePretService", e);
    }
}
```

## 4. Servlets à Créer dans le Centralisateur

### 4.1 SimulationPretServlet.java

```java
package com.banque.centralisateur.servlet;

@WebServlet("/simulation-pret")
public class SimulationPretServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Récupérer les types de prêts disponibles
        TypePretServiceRemote typePretService = EJBClientFactory.getTypePretService();
        List<TypePretDTO> typesPrets = typePretService.listerTypesPretsActifs();
        
        request.setAttribute("typesPrets", typesPrets);
        request.getRequestDispatcher("/WEB-INF/templates/simulation-pret.html")
               .forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Récupérer les paramètres
        BigDecimal montant = new BigDecimal(request.getParameter("montant"));
        Integer duree = Integer.parseInt(request.getParameter("duree"));
        BigDecimal taux = new BigDecimal(request.getParameter("taux"));
        
        // Appeler le service de simulation
        PretServiceRemote pretService = EJBClientFactory.getPretService();
        SimulationPretDTO params = SimulationPretDTO.builder()
                .montantDemande(montant)
                .dureeMois(duree)
                .tauxInteretAnnuel(taux)
                .build();
        
        SimulationPretDTO resultat = pretService.simulerPret(params);
        
        request.setAttribute("simulation", resultat);
        request.getRequestDispatcher("/WEB-INF/templates/resultat-simulation.html")
               .forward(request, response);
    }
}
```

### 4.2 DemandePretServlet.java

```java
package com.banque.centralisateur.servlet;

@WebServlet("/demande-pret")
public class DemandePretServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Afficher le formulaire de demande
        TypePretServiceRemote typePretService = EJBClientFactory.getTypePretService();
        List<TypePretDTO> typesPrets = typePretService.listerTypesPretsActifs();
        
        request.setAttribute("typesPrets", typesPrets);
        request.getRequestDispatcher("/WEB-INF/templates/demande-pret.html")
               .forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Récupérer le client depuis la session
        ClientDTO client = (ClientDTO) request.getSession().getAttribute("client");
        
        // Créer la demande de prêt
        PretDTO pretDTO = PretDTO.builder()
                .idClient(client.getIdClient())
                .idTypePret(Long.parseLong(request.getParameter("typePret")))
                .montantDemande(new BigDecimal(request.getParameter("montant")))
                .dureeMois(Integer.parseInt(request.getParameter("duree")))
                .build();
        
        PretServiceRemote pretService = EJBClientFactory.getPretService();
        PretDTO pretCree = pretService.creerDemandePret(pretDTO);
        
        response.sendRedirect("mes-prets?success=demande_creee");
    }
}
```

### 4.3 MesPretsServlet.java

```java
package com.banque.centralisateur.servlet;

@WebServlet("/mes-prets")
public class MesPretsServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Récupérer le client depuis la session
        ClientDTO client = (ClientDTO) request.getSession().getAttribute("client");
        
        // Récupérer ses prêts
        PretServiceRemote pretService = EJBClientFactory.getPretService();
        List<PretDTO> prets = pretService.listerPretsParClient(client.getIdClient());
        
        request.setAttribute("prets", prets);
        request.getRequestDispatcher("/WEB-INF/templates/mes-prets.html")
               .forward(request, response);
    }
}
```

### 4.4 TableauAmortissementServlet.java

```java
package com.banque.centralisateur.servlet;

@WebServlet("/tableau-amortissement")
public class TableauAmortissementServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        Long idPret = Long.parseLong(request.getParameter("idPret"));
        
        // Récupérer le prêt
        PretServiceRemote pretService = EJBClientFactory.getPretService();
        PretDTO pret = pretService.rechercherPretParId(idPret);
        
        // Récupérer le tableau d'amortissement
        EcheanceServiceRemote echeanceService = EJBClientFactory.getEcheanceService();
        List<EcheanceDTO> echeances = echeanceService.obtenirTableauAmortissement(idPret);
        
        request.setAttribute("pret", pret);
        request.setAttribute("echeances", echeances);
        request.getRequestDispatcher("/WEB-INF/templates/tableau-amortissement.html")
               .forward(request, response);
    }
}
```

### 4.5 RemboursementServlet.java

```java
package com.banque.centralisateur.servlet;

@WebServlet("/remboursement")
public class RemboursementServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        Long idEcheance = Long.parseLong(request.getParameter("idEcheance"));
        Long idCompte = Long.parseLong(request.getParameter("idCompte"));
        BigDecimal montant = new BigDecimal(request.getParameter("montant"));
        
        // Créer le remboursement
        RemboursementDTO remboursementDTO = RemboursementDTO.builder()
                .idEcheance(idEcheance)
                .idCompte(idCompte)
                .montant(montant)
                .montantEcheance(montant)
                .montantPenalite(BigDecimal.ZERO)
                .typePaiement(TypePaiement.VIREMENT)
                .build();
        
        EcheanceServiceRemote echeanceService = EJBClientFactory.getEcheanceService();
        echeanceService.enregistrerRemboursement(remboursementDTO);
        
        response.sendRedirect("mes-prets?success=paiement_effectue");
    }
}
```

## 5. Templates Thymeleaf à Créer

### 5.1 simulation-pret.html
- Formulaire de saisie des paramètres de simulation
- Sélection du type de prêt
- Champs: montant, durée

### 5.2 resultat-simulation.html
- Affichage de la mensualité calculée
- Tableau d'amortissement détaillé
- Coût total du crédit
- Bouton pour faire une demande basée sur cette simulation

### 5.3 demande-pret.html
- Formulaire de demande de prêt
- Sélection du type de prêt
- Validation côté client

### 5.4 mes-prets.html
- Liste des prêts du client
- Statut de chaque prêt
- Liens vers le tableau d'amortissement

### 5.5 tableau-amortissement.html
- Affichage du tableau d'amortissement complet
- Détail par échéance: capital, intérêt, capital restant
- Statut de chaque échéance
- Bouton de paiement pour les échéances impayées

## 6. Mise à Jour du Dashboard

Ajouter dans `dashboard.html` :

```html
<div class="card">
    <h3>Mes Prêts</h3>
    <ul>
        <li><a href="/simulation-pret">Simuler un prêt</a></li>
        <li><a href="/demande-pret">Demander un prêt</a></li>
        <li><a href="/mes-prets">Mes demandes de prêt</a></li>
    </ul>
</div>
```

## 7. Ordre de Déploiement

1. Déployer le module SituationBancaire
2. Déployer le module Prêt
3. Redéployer le Centralisateur avec les nouvelles dépendances
4. Tester les appels EJB Remote

## 8. Test de l'Intégration

1. Se connecter au Centralisateur
2. Aller dans "Simuler un prêt"
3. Saisir les paramètres et valider
4. Vérifier que le tableau d'amortissement s'affiche
5. Créer une demande de prêt
6. Vérifier la liste des prêts

## 9. Troubleshooting

### Erreur: "Failed to create proxy"
- Vérifier que le module Prêt est bien déployé
- Vérifier les noms JNDI dans EJBClientFactory

### Erreur: "ClassNotFoundException"
- Ajouter la dépendance client du module Prêt dans le Centralisateur

### Erreur de connexion à la base de données
- Vérifier que PostgreSQL est démarré
- Vérifier les credentials dans `pret-ds.xml`
- Vérifier que la base `pret_db` existe

## 10. Sécurité

- Toujours vérifier que le client connecté est propriétaire du prêt avant d'afficher les détails
- Valider les montants et durées côté serveur
- Protéger les endpoints avec des filtres de session
