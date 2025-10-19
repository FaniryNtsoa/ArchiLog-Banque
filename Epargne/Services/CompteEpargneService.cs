using Epargne.Models.DTOs;
using Epargne.Models.Entities;
using Epargne.Models.Enums;
using Epargne.Repositories;
using Epargne.Utilities;

namespace Epargne.Services;

/// <summary>
/// Service pour la gestion des comptes épargne
/// Implémente toutes les règles métier
/// </summary>
public interface ICompteEpargneService
{
    Task<CompteEpargneDTO?> GetCompteByIdAsync(int id);
    Task<CompteEpargneDTO?> GetCompteByNumeroAsync(string numeroCompte);
    Task<IEnumerable<CompteEpargneDTO>> GetComptesByClientIdAsync(long clientId);
    Task<CompteEpargneDTO> CreerCompteEpargneAsync(CreationCompteEpargneDTO creation);
    Task<OperationEpargneDTO> EffectuerDepotAsync(DepotDTO depot);
    Task<OperationEpargneDTO> EffectuerRetraitAsync(RetraitDTO retrait);
    Task<IEnumerable<OperationEpargneDTO>> GetHistoriqueOperationsAsync(int compteId);
    Task<decimal> GetSoldeDisponibleAsync(int compteId);
}

public class CompteEpargneService : ICompteEpargneService
{
    private readonly ICompteEpargneRepository _compteRepository;
    private readonly IClientRepository _clientRepository;
    private readonly ITypeCompteEpargneRepository _typeCompteRepository;
    private readonly IOperationEpargneRepository _operationRepository;
    private readonly ILogger<CompteEpargneService> _logger;

    public CompteEpargneService(
        ICompteEpargneRepository compteRepository,
        IClientRepository clientRepository,
        ITypeCompteEpargneRepository typeCompteRepository,
        IOperationEpargneRepository operationRepository,
        ILogger<CompteEpargneService> logger)
    {
        _compteRepository = compteRepository;
        _clientRepository = clientRepository;
        _typeCompteRepository = typeCompteRepository;
        _operationRepository = operationRepository;
        _logger = logger;
    }

    public async Task<CompteEpargneDTO?> GetCompteByIdAsync(int id)
    {
        var compte = await _compteRepository.GetByIdAsync(id);
        return compte != null ? MapToDTO(compte) : null;
    }

    public async Task<CompteEpargneDTO?> GetCompteByNumeroAsync(string numeroCompte)
    {
        var compte = await _compteRepository.GetByNumeroCompteAsync(numeroCompte);
        return compte != null ? MapToDTO(compte) : null;
    }

    public async Task<IEnumerable<CompteEpargneDTO>> GetComptesByClientIdAsync(long clientId)
    {
        var comptes = await _compteRepository.GetByClientIdAsync(clientId);
        return comptes.Select(MapToDTO);
    }

    public async Task<CompteEpargneDTO> CreerCompteEpargneAsync(CreationCompteEpargneDTO creation)
    {
        _logger.LogInformation($"Création d'un compte épargne pour le client {creation.IdClient}");

        // Vérifier que le client existe
        var client = await _clientRepository.GetByIdAsync(creation.IdClient);
        if (client == null)
        {
            throw new InvalidOperationException("Client non trouvé");
        }

        // Vérifier que le type de compte existe et est actif
        var typeCompte = await _typeCompteRepository.GetByIdAsync(creation.IdTypeCompte);
        if (typeCompte == null)
        {
            throw new InvalidOperationException("Type de compte non trouvé");
        }

        if (!typeCompte.Actif)
        {
            throw new InvalidOperationException("Ce type de compte n'est plus disponible");
        }

        // RÈGLE MÉTIER: Vérifier le dépôt initial minimum
        if (creation.DepotInitial < typeCompte.DepotInitialMin)
        {
            throw new InvalidOperationException(
                $"Le dépôt initial doit être d'au moins {typeCompte.DepotInitialMin:C}");
        }

        // RÈGLE MÉTIER: Vérifier le plafond de dépôt
        if (creation.DepotInitial > typeCompte.PlafondDepot)
        {
            throw new InvalidOperationException(
                $"Le dépôt ne peut pas dépasser {typeCompte.PlafondDepot:C}");
        }

        // Créer le compte
        var compte = new CompteEpargne
        {
            IdClient = creation.IdClient,
            IdTypeCompte = creation.IdTypeCompte,
            NumeroCompte = NumeroGenerator.GenererNumeroCompteEpargne(),
            LibelleCompte = creation.LibelleCompte ?? $"Compte {typeCompte.Libelle}",
            Solde = creation.DepotInitial,
            SoldeDisponible = creation.DepotInitial,
            SoldeMinHistorique = creation.DepotInitial,
            DateOuverture = DateOnly.FromDateTime(DateTime.UtcNow),
            DateDerniereOperation = DateOnly.FromDateTime(DateTime.UtcNow),
            Statut = CompteStatut.ACTIF,
            DateCreation = DateTime.UtcNow,
            DateModification = DateTime.UtcNow
        };

        var compteCreated = await _compteRepository.CreateAsync(compte);

        // Créer l'opération de dépôt initial
        var operation = new OperationEpargne
        {
            IdCompte = compteCreated.IdCompte,
            TypeOperation = OperationType.DEPOT,
            Montant = creation.DepotInitial,
            SoldeAvant = 0,
            SoldeApres = creation.DepotInitial,
            Description = "Dépôt initial lors de l'ouverture du compte",
            ReferenceOperation = NumeroGenerator.GenererReferenceOperation(),
            DateOperation = DateTime.UtcNow
        };

        await _operationRepository.CreateAsync(operation);

        _logger.LogInformation($"Compte épargne créé : {compteCreated.NumeroCompte}");

        // Recharger le compte avec ses relations
        var compteFinal = await _compteRepository.GetByIdAsync(compteCreated.IdCompte);
        return MapToDTO(compteFinal!);
    }

    public async Task<OperationEpargneDTO> EffectuerDepotAsync(DepotDTO depot)
    {
        var compte = await _compteRepository.GetByIdAsync(depot.IdCompte);
        if (compte == null)
        {
            throw new InvalidOperationException("Compte non trouvé");
        }

        if (compte.Statut != CompteStatut.ACTIF)
        {
            throw new InvalidOperationException("Le compte n'est pas actif");
        }

        // RÈGLE MÉTIER: Vérifier le plafond de dépôt
        if (compte.Solde + depot.Montant > compte.TypeCompte.PlafondDepot)
        {
            throw new InvalidOperationException(
                $"Le solde ne peut pas dépasser {compte.TypeCompte.PlafondDepot:C}");
        }

        var soldeAvant = compte.Solde;
        compte.Solde += depot.Montant;
        compte.SoldeDisponible += depot.Montant;
        compte.DateDerniereOperation = DateOnly.FromDateTime(DateTime.UtcNow);

        // Mettre à jour le solde minimum historique si nécessaire
        if (compte.Solde < compte.SoldeMinHistorique)
        {
            compte.SoldeMinHistorique = compte.Solde;
        }

        await _compteRepository.UpdateAsync(compte);

        // Créer l'opération
        var operation = new OperationEpargne
        {
            IdCompte = depot.IdCompte,
            TypeOperation = OperationType.DEPOT,
            Montant = depot.Montant,
            SoldeAvant = soldeAvant,
            SoldeApres = compte.Solde,
            Description = depot.Description ?? "Dépôt",
            ReferenceOperation = NumeroGenerator.GenererReferenceOperation(),
            DateOperation = DateTime.UtcNow
        };

        var operationCreated = await _operationRepository.CreateAsync(operation);
        _logger.LogInformation($"Dépôt de {depot.Montant:C} effectué sur le compte {compte.NumeroCompte}");

        return MapOperationToDTO(operationCreated);
    }

    public async Task<OperationEpargneDTO> EffectuerRetraitAsync(RetraitDTO retrait)
    {
        var compte = await _compteRepository.GetByIdAsync(retrait.IdCompte);
        if (compte == null)
        {
            throw new InvalidOperationException("Compte non trouvé");
        }

        if (compte.Statut != CompteStatut.ACTIF)
        {
            throw new InvalidOperationException("Le compte n'est pas actif");
        }

        // RÈGLE MÉTIER: Contrôle des plafonds de retrait (% du solde)
        var retraitMax = compte.Solde * (compte.TypeCompte.RetraitMaxPourcentage / 100);
        if (retrait.Montant > retraitMax)
        {
            throw new InvalidOperationException(
                $"Le retrait ne peut pas dépasser {compte.TypeCompte.RetraitMaxPourcentage}% du solde ({retraitMax:C})");
        }

        // RÈGLE MÉTIER: Vérification solde minimum après retrait
        var soldeApresRetrait = compte.Solde - retrait.Montant;
        if (soldeApresRetrait < compte.TypeCompte.SoldeMinObligatoire)
        {
            throw new InvalidOperationException(
                $"Le solde après retrait doit être d'au moins {compte.TypeCompte.SoldeMinObligatoire:C}");
        }

        var soldeAvant = compte.Solde;
        compte.Solde -= retrait.Montant;
        compte.SoldeDisponible -= retrait.Montant;
        compte.DateDerniereOperation = DateOnly.FromDateTime(DateTime.UtcNow);

        // Mettre à jour le solde minimum historique si nécessaire
        if (compte.Solde < compte.SoldeMinHistorique)
        {
            compte.SoldeMinHistorique = compte.Solde;
        }

        await _compteRepository.UpdateAsync(compte);

        // Créer l'opération
        var operation = new OperationEpargne
        {
            IdCompte = retrait.IdCompte,
            TypeOperation = OperationType.RETRAIT,
            Montant = retrait.Montant,
            SoldeAvant = soldeAvant,
            SoldeApres = compte.Solde,
            Description = retrait.Description ?? "Retrait",
            ReferenceOperation = NumeroGenerator.GenererReferenceOperation(),
            DateOperation = DateTime.UtcNow
        };

        var operationCreated = await _operationRepository.CreateAsync(operation);
        _logger.LogInformation($"Retrait de {retrait.Montant:C} effectué sur le compte {compte.NumeroCompte}");

        return MapOperationToDTO(operationCreated);
    }

    public async Task<IEnumerable<OperationEpargneDTO>> GetHistoriqueOperationsAsync(int compteId)
    {
        var operations = await _operationRepository.GetByCompteIdAsync(compteId);
        return operations.Select(MapOperationToDTO);
    }

    public async Task<decimal> GetSoldeDisponibleAsync(int compteId)
    {
        var compte = await _compteRepository.GetByIdAsync(compteId);
        if (compte == null)
        {
            throw new InvalidOperationException("Compte non trouvé");
        }

        return compte.SoldeDisponible;
    }

    private static CompteEpargneDTO MapToDTO(CompteEpargne compte)
    {
        return new CompteEpargneDTO
        {
            IdCompte = compte.IdCompte,
            IdClient = compte.IdClient,
            IdTypeCompte = compte.IdTypeCompte,
            NumeroCompte = compte.NumeroCompte,
            LibelleCompte = compte.LibelleCompte,
            Solde = compte.Solde,
            SoldeDisponible = compte.SoldeDisponible,
            SoldeMinHistorique = compte.SoldeMinHistorique,
            DateOuverture = compte.DateOuverture,
            DateDernierCalculInteret = compte.DateDernierCalculInteret,
            DateDerniereOperation = compte.DateDerniereOperation,
            Statut = compte.Statut.ToString(),
            MotifFermeture = compte.MotifFermeture,
            DateFermeture = compte.DateFermeture,
            TypeCompte = compte.TypeCompte != null ? MapTypeCompteToDTO(compte.TypeCompte) : null,
            Client = compte.Client != null ? MapClientToDTO(compte.Client) : null
        };
    }

    private static TypeCompteEpargneDTO MapTypeCompteToDTO(TypeCompteEpargne type)
    {
        return new TypeCompteEpargneDTO
        {
            IdTypeCompte = type.IdTypeCompte,
            CodeType = type.CodeType,
            Libelle = type.Libelle,
            Description = type.Description,
            TauxInteretAnnuel = type.TauxInteretAnnuel,
            DepotInitialMin = type.DepotInitialMin,
            SoldeMinObligatoire = type.SoldeMinObligatoire,
            PlafondDepot = type.PlafondDepot,
            RetraitMaxPourcentage = type.RetraitMaxPourcentage,
            FraisTenueCompte = type.FraisTenueCompte,
            PeriodiciteCalculInteret = type.PeriodiciteCalculInteret.ToString(),
            Actif = type.Actif
        };
    }

    private static ClientDTO MapClientToDTO(Client client)
    {
        return new ClientDTO
        {
            IdClient = client.IdClient,
            NumeroClient = client.NumeroClient,
            Nom = client.Nom,
            Prenom = client.Prenom,
            Email = client.Email
        };
    }

    private static OperationEpargneDTO MapOperationToDTO(OperationEpargne operation)
    {
        return new OperationEpargneDTO
        {
            IdOperation = operation.IdOperation,
            IdCompte = operation.IdCompte,
            TypeOperation = operation.TypeOperation.ToString(),
            Montant = operation.Montant,
            SoldeAvant = operation.SoldeAvant,
            SoldeApres = operation.SoldeApres,
            Description = operation.Description,
            ReferenceOperation = operation.ReferenceOperation,
            DateOperation = operation.DateOperation
        };
    }
}
