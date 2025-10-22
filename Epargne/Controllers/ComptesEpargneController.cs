using Microsoft.AspNetCore.Mvc;
using Epargne.Models.DTOs;
using Epargne.Services;

namespace Epargne.Controllers;

/// <summary>
/// Contrôleur pour la gestion des comptes épargne
/// </summary>
[ApiController]
[Route("api/[controller]")]
public class ComptesEpargneController : ControllerBase
{
    private readonly ICompteEpargneService _compteService;
    private readonly IInteretService _interetService;
    private readonly ILogger<ComptesEpargneController> _logger;

    public ComptesEpargneController(
        ICompteEpargneService compteService,
        IInteretService interetService,
        ILogger<ComptesEpargneController> logger)
    {
        _compteService = compteService;
        _interetService = interetService;
        _logger = logger;
    }

    /// <summary>
    /// Récupère tous les comptes épargne (pour administration)
    /// </summary>
    [HttpGet]
    public async Task<ActionResult<IEnumerable<CompteEpargneDTO>>> GetAllComptes()
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
    /// Récupère un compte épargne par son ID
    /// </summary>
    [HttpGet("{id}")]
    public async Task<ActionResult<CompteEpargneDTO>> GetCompteById(int id)
    {
        try
        {
            var compte = await _compteService.GetCompteByIdAsync(id);
            if (compte == null)
            {
                return NotFound(new { message = "Compte non trouvé" });
            }
            return Ok(compte);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors de la récupération du compte {id}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Récupère un compte par son numéro
    /// </summary>
    [HttpGet("numero/{numeroCompte}")]
    public async Task<ActionResult<CompteEpargneDTO>> GetCompteByNumero(string numeroCompte)
    {
        try
        {
            var compte = await _compteService.GetCompteByNumeroAsync(numeroCompte);
            if (compte == null)
            {
                return NotFound(new { message = "Compte non trouvé" });
            }
            return Ok(compte);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors de la récupération du compte {numeroCompte}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Récupère tous les comptes d'un client
    /// </summary>
    [HttpGet("client/{clientId}")]
    public async Task<ActionResult<IEnumerable<CompteEpargneDTO>>> GetComptesByClientId(long clientId)
    {
        try
        {
            var comptes = await _compteService.GetComptesByClientIdAsync(clientId);
            return Ok(comptes);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors de la récupération des comptes du client {clientId}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Crée un nouveau compte épargne (avec dépôt initial)
    /// </summary>
    [HttpPost]
    public async Task<ActionResult<CompteEpargneDTO>> CreerCompteEpargne([FromBody] CreationCompteEpargneDTO creation)
    {
        try
        {
            var compte = await _compteService.CreerCompteEpargneAsync(creation);
            return CreatedAtAction(nameof(GetCompteById), new { id = compte.IdCompte }, compte);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erreur lors de la création du compte");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Effectue un dépôt sur un compte
    /// </summary>
    [HttpPost("{id}/depot")]
    public async Task<ActionResult<OperationEpargneDTO>> EffectuerDepot(int id, [FromBody] DepotDTO depot)
    {
        try
        {
            depot.IdCompte = id;
            var operation = await _compteService.EffectuerDepotAsync(depot);
            return Ok(operation);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors du dépôt sur le compte {id}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Effectue un retrait sur un compte
    /// </summary>
    [HttpPost("{id}/retrait")]
    public async Task<ActionResult<OperationEpargneDTO>> EffectuerRetrait(int id, [FromBody] RetraitDTO retrait)
    {
        try
        {
            retrait.IdCompte = id;
            var operation = await _compteService.EffectuerRetraitAsync(retrait);
            return Ok(operation);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors du retrait sur le compte {id}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Récupère l'historique des opérations d'un compte
    /// </summary>
    [HttpGet("{id}/operations")]
    public async Task<ActionResult<IEnumerable<OperationEpargneDTO>>> GetHistoriqueOperations(int id)
    {
        try
        {
            var operations = await _compteService.GetHistoriqueOperationsAsync(id);
            return Ok(operations);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors de la récupération de l'historique du compte {id}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Récupère le solde disponible d'un compte
    /// </summary>
    [HttpGet("{id}/solde-disponible")]
    public async Task<ActionResult<decimal>> GetSoldeDisponible(int id)
    {
        try
        {
            var solde = await _compteService.GetSoldeDisponibleAsync(id);
            return Ok(new { soldeDisponible = solde });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors de la récupération du solde disponible du compte {id}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Récupère l'historique des intérêts d'un compte
    /// </summary>
    [HttpGet("{id}/interets")]
    public async Task<ActionResult<IEnumerable<InteretEpargneDTO>>> GetHistoriqueInterets(int id)
    {
        try
        {
            var interets = await _interetService.GetHistoriqueInteretsAsync(id);
            return Ok(interets);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors de la récupération des intérêts du compte {id}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Calcule les intérêts pour une période donnée
    /// </summary>
    [HttpPost("{id}/calculer-interets")]
    public async Task<ActionResult<InteretEpargneDTO>> CalculerInterets(
        int id,
        [FromQuery] DateOnly dateDebut,
        [FromQuery] DateOnly dateFin)
    {
        try
        {
            var interet = await _interetService.CalculerInteretsPeriodeAsync(id, dateDebut, dateFin);
            return Ok(interet);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors du calcul des intérêts du compte {id}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Capitalise les intérêts d'un compte
    /// </summary>
    [HttpPost("{id}/capitaliser-interets")]
    public async Task<ActionResult> CapitaliserInterets(int id)
    {
        try
        {
            await _interetService.CapitaliserInteretsAsync(id);
            return Ok(new { message = "Intérêts capitalisés avec succès" });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors de la capitalisation des intérêts du compte {id}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }
}
