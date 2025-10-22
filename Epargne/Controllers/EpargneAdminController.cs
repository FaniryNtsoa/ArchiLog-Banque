using Microsoft.AspNetCore.Mvc;
using Epargne.Models.DTOs;
using Epargne.Services;

namespace Epargne.Controllers;

/// <summary>
/// Contrôleur pour les opérations administratives d'épargne
/// Toutes les opérations sont effectuées par un administrateur connecté via le centralisateur
/// </summary>
[ApiController]
[Route("api/admin/[controller]")]
public class EpargneAdminController : ControllerBase
{
    private readonly ICompteEpargneService _compteService;
    private readonly IClientService _clientService;
    private readonly ITypeCompteEpargneService _typeCompteService;
    private readonly ILogger<EpargneAdminController> _logger;

    public EpargneAdminController(
        ICompteEpargneService compteService,
        IClientService clientService,
        ITypeCompteEpargneService typeCompteService,
        ILogger<EpargneAdminController> logger)
    {
        _compteService = compteService;
        _clientService = clientService;
        _typeCompteService = typeCompteService;
        _logger = logger;
    }

    /// <summary>
    /// Récupère tous les clients avec leurs comptes épargne
    /// </summary>
    [HttpGet("clients-avec-comptes")]
    public async Task<ActionResult<IEnumerable<object>>> GetClientsAvecComptes()
    {
        try
        {
            var clients = await _clientService.GetAllClientsAsync();
            var result = new List<object>();

            foreach (var client in clients)
            {
                var comptes = await _compteService.GetComptesByClientIdAsync(client.IdClient!.Value);
                result.Add(new
                {
                    client = client,
                    comptes = comptes
                });
            }

            return Ok(result);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erreur lors de la récupération des clients avec comptes");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Crée un compte épargne pour un client (opération administrative)
    /// </summary>
    [HttpPost("creer-compte")]
    public async Task<ActionResult<CompteEpargneDTO>> CreerCompteEpargneAdmin([FromBody] CreationCompteEpargneDTO creation)
    {
        try
        {
            var compte = await _compteService.CreerCompteEpargneAsync(creation);
            return CreatedAtAction(nameof(GetCompteDetails), new { id = compte.IdCompte }, compte);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erreur lors de la création du compte par l'administrateur");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Effectue un dépôt pour un client (opération administrative)
    /// </summary>
    [HttpPost("depot")]
    public async Task<ActionResult<OperationEpargneDTO>> EffectuerDepotAdmin([FromBody] DepotDTO depot)
    {
        try
        {
            var operation = await _compteService.EffectuerDepotAsync(depot);
            return Ok(operation);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erreur lors du dépôt par l'administrateur");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Effectue un retrait pour un client (opération administrative)
    /// </summary>
    [HttpPost("retrait")]
    public async Task<ActionResult<OperationEpargneDTO>> EffectuerRetraitAdmin([FromBody] RetraitDTO retrait)
    {
        try
        {
            var operation = await _compteService.EffectuerRetraitAsync(retrait);
            return Ok(operation);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erreur lors du retrait par l'administrateur");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Récupère les détails d'un compte avec l'historique des opérations
    /// </summary>
    [HttpGet("compte/{id}")]
    public async Task<ActionResult<object>> GetCompteDetails(int id)
    {
        try
        {
            var compte = await _compteService.GetCompteByIdAsync(id);
            if (compte == null)
            {
                return NotFound(new { message = "Compte non trouvé" });
            }

            var operations = await _compteService.GetHistoriqueOperationsAsync(id);
            var soldeDisponible = await _compteService.GetSoldeDisponibleAsync(id);

            return Ok(new
            {
                compte = compte,
                operations = operations,
                soldeDisponible = soldeDisponible
            });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors de la récupération des détails du compte {id}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Récupère la liste de tous les comptes épargne avec leurs détails
    /// </summary>
    [HttpGet("tous-comptes")]
    public async Task<ActionResult<IEnumerable<CompteEpargneDTO>>> GetTousLesComptes()
    {
        try
        {
            var comptes = await _compteService.GetAllComptesAsync();
            return Ok(comptes);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erreur lors de la récupération de tous les comptes");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Récupère les statistiques des comptes épargne
    /// </summary>
    [HttpGet("statistiques")]
    public async Task<ActionResult<object>> GetStatistiques()
    {
        try
        {
            var comptes = await _compteService.GetAllComptesAsync();
            var clients = await _clientService.GetAllClientsAsync();

            var statistiques = new
            {
                NombreClients = clients.Count(),
                NombreComptes = comptes.Count(),
                SoldeTotal = comptes.Sum(c => c.Solde),
                SoldeMoyen = comptes.Any() ? comptes.Average(c => c.Solde) : 0,
                ComptesParStatut = comptes.GroupBy(c => c.Statut)
                    .ToDictionary(g => g.Key, g => g.Count())
            };

            return Ok(statistiques);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erreur lors de la récupération des statistiques");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }
}