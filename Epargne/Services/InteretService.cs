using Epargne.Models.DTOs;
using Epargne.Models.Entities;
using Epargne.Models.Enums;
using Epargne.Repositories;
using Epargne.Utilities;

namespace Epargne.Services;

/// <summary>
/// Service pour le calcul et la capitalisation des intérêts
/// </summary>
public interface IInteretService
{
    Task<InteretEpargneDTO> CalculerInteretsPeriodeAsync(int compteId, DateOnly dateDebut, DateOnly dateFin);
    Task CapitaliserInteretsAsync(int compteId);
    Task<IEnumerable<InteretEpargneDTO>> GetHistoriqueInteretsAsync(int compteId);
    Task CalculerEtCapitaliserInteretsTousComptesAsync();
}

public class InteretService : IInteretService
{
    private readonly ICompteEpargneRepository _compteRepository;
    private readonly IInteretEpargneRepository _interetRepository;
    private readonly IOperationEpargneRepository _operationRepository;
    private readonly ILogger<InteretService> _logger;

    public InteretService(
        ICompteEpargneRepository compteRepository,
        IInteretEpargneRepository interetRepository,
        IOperationEpargneRepository operationRepository,
        ILogger<InteretService> logger)
    {
        _compteRepository = compteRepository;
        _interetRepository = interetRepository;
        _operationRepository = operationRepository;
        _logger = logger;
    }

    public async Task<InteretEpargneDTO> CalculerInteretsPeriodeAsync(int compteId, DateOnly dateDebut, DateOnly dateFin)
    {
        var compte = await _compteRepository.GetByIdAsync(compteId);
        if (compte == null)
        {
            throw new InvalidOperationException("Compte non trouvé");
        }

        // Calcul du nombre de jours
        int nombreJours = InteretCalculator.CalculerNombreJours(dateDebut, dateFin);

        // Calcul du solde moyen (simplifié: on utilise le solde actuel)
        // Dans une vraie implémentation, on devrait calculer le solde moyen quotidien
        decimal soldeMoyen = compte.Solde;

        // Calcul des intérêts
        decimal interetCouru = InteretCalculator.CalculerInterets(
            soldeMoyen,
            compte.TypeCompte.TauxInteretAnnuel,
            nombreJours
        );

        // Dans une vraie implémentation, on appliquerait les impôts
        decimal interetNet = interetCouru;

        // Créer l'enregistrement d'intérêt
        var interet = new InteretEpargne
        {
            IdCompte = compteId,
            PeriodeDebut = dateDebut,
            PeriodeFin = dateFin,
            SoldeMoyenPeriode = soldeMoyen,
            TauxInteretApplique = compte.TypeCompte.TauxInteretAnnuel,
            JoursPeriode = nombreJours,
            InteretCouru = interetCouru,
            InteretNet = interetNet,
            DateCalcul = DateTime.UtcNow,
            Statut = InteretStatut.CALCULE
        };

        var interetCreated = await _interetRepository.CreateAsync(interet);
        _logger.LogInformation($"Intérêts calculés pour le compte {compte.NumeroCompte}: {interetNet:C}");

        return MapToDTO(interetCreated);
    }

    public async Task CapitaliserInteretsAsync(int compteId)
    {
        var compte = await _compteRepository.GetByIdAsync(compteId);
        if (compte == null)
        {
            throw new InvalidOperationException("Compte non trouvé");
        }

        // Récupérer tous les intérêts calculés mais non capitalisés
        var interetsNonCapitalises = (await _interetRepository.GetByCompteIdAsync(compteId))
            .Where(i => i.Statut == InteretStatut.CALCULE)
            .ToList();

        if (!interetsNonCapitalises.Any())
        {
            _logger.LogInformation($"Aucun intérêt à capitaliser pour le compte {compte.NumeroCompte}");
            return;
        }

        decimal totalInterets = interetsNonCapitalises.Sum(i => i.InteretNet);

        // Créditer les intérêts sur le compte
        var soldeAvant = compte.Solde;
        compte.Solde += totalInterets;
        compte.SoldeDisponible += totalInterets;
        compte.DateDernierCalculInteret = DateOnly.FromDateTime(DateTime.UtcNow);

        await _compteRepository.UpdateAsync(compte);

        // Créer une opération de capitalisation
        var operation = new OperationEpargne
        {
            IdCompte = compteId,
            TypeOperation = OperationType.INTERETS_CAPITALISES,
            Montant = totalInterets,
            SoldeAvant = soldeAvant,
            SoldeApres = compte.Solde,
            Description = $"Capitalisation des intérêts ({interetsNonCapitalises.Count} période(s))",
            ReferenceOperation = NumeroGenerator.GenererReferenceOperation(),
            DateOperation = DateTime.UtcNow
        };

        await _operationRepository.CreateAsync(operation);

        // Mettre à jour le statut des intérêts
        foreach (var interet in interetsNonCapitalises)
        {
            interet.Statut = InteretStatut.CAPITALISE;
            interet.DateCreditInteret = DateOnly.FromDateTime(DateTime.UtcNow);
            await _interetRepository.UpdateAsync(interet);
        }

        _logger.LogInformation($"Intérêts capitalisés sur le compte {compte.NumeroCompte}: {totalInterets:C}");
    }

    public async Task<IEnumerable<InteretEpargneDTO>> GetHistoriqueInteretsAsync(int compteId)
    {
        var interets = await _interetRepository.GetByCompteIdAsync(compteId);
        return interets.Select(MapToDTO);
    }

    public async Task CalculerEtCapitaliserInteretsTousComptesAsync()
    {
        _logger.LogInformation("Calcul et capitalisation des intérêts pour tous les comptes");

        var comptes = await _compteRepository.GetAllAsync();
        var dateActuelle = DateOnly.FromDateTime(DateTime.UtcNow);

        foreach (var compte in comptes.Where(c => c.Statut == CompteStatut.ACTIF))
        {
            try
            {
                // Déterminer la période de calcul
                var dateDebut = compte.DateDernierCalculInteret ?? compte.DateOuverture;
                var dateFin = dateActuelle;

                // Vérifier si c'est le moment de capitaliser selon la périodicité
                bool doitCapitaliser = InteretCalculator.DoitCapitaliser(
                    dateDebut,
                    dateActuelle,
                    compte.TypeCompte.PeriodiciteCalculInteret.ToString()
                );

                if (doitCapitaliser && dateDebut < dateFin)
                {
                    // Calculer les intérêts
                    await CalculerInteretsPeriodeAsync(compte.IdCompte, dateDebut, dateFin);

                    // Capitaliser immédiatement
                    await CapitaliserInteretsAsync(compte.IdCompte);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, $"Erreur lors du calcul des intérêts pour le compte {compte.NumeroCompte}");
            }
        }

        _logger.LogInformation("Calcul et capitalisation des intérêts terminés");
    }

    private static InteretEpargneDTO MapToDTO(InteretEpargne interet)
    {
        return new InteretEpargneDTO
        {
            IdInteret = interet.IdInteret,
            IdCompte = interet.IdCompte,
            PeriodeDebut = interet.PeriodeDebut,
            PeriodeFin = interet.PeriodeFin,
            SoldeMoyenPeriode = interet.SoldeMoyenPeriode,
            TauxInteretApplique = interet.TauxInteretApplique,
            JoursPeriode = interet.JoursPeriode,
            InteretCouru = interet.InteretCouru,
            InteretNet = interet.InteretNet,
            DateCalcul = interet.DateCalcul,
            DateCreditInteret = interet.DateCreditInteret,
            Statut = interet.Statut.ToString()
        };
    }
}
