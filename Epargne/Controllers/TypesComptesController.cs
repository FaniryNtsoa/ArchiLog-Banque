using Microsoft.AspNetCore.Mvc;
using Epargne.Models.DTOs;
using Epargne.Services;

namespace Epargne.Controllers;

/// <summary>
/// Contrôleur pour les types de comptes épargne
/// </summary>
[ApiController]
[Route("api/[controller]")]
public class TypesComptesController : ControllerBase
{
    private readonly ITypeCompteEpargneService _service;
    private readonly ILogger<TypesComptesController> _logger;

    public TypesComptesController(ITypeCompteEpargneService service, ILogger<TypesComptesController> logger)
    {
        _service = service;
        _logger = logger;
    }

    /// <summary>
    /// Récupère tous les types de comptes épargne
    /// </summary>
    [HttpGet]
    public async Task<ActionResult<IEnumerable<TypeCompteEpargneDTO>>> GetAllTypesComptes()
    {
        try
        {
            var types = await _service.GetAllTypesComptesAsync();
            return Ok(types);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erreur lors de la récupération des types de comptes");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Récupère uniquement les types de comptes actifs
    /// </summary>
    [HttpGet("actifs")]
    public async Task<ActionResult<IEnumerable<TypeCompteEpargneDTO>>> GetTypesComptesActifs()
    {
        try
        {
            var types = await _service.GetTypesComptesActifsAsync();
            return Ok(types);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erreur lors de la récupération des types de comptes actifs");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Récupère un type de compte par son ID
    /// </summary>
    [HttpGet("{id}")]
    public async Task<ActionResult<TypeCompteEpargneDTO>> GetTypeCompteById(int id)
    {
        try
        {
            var type = await _service.GetTypeCompteByIdAsync(id);
            if (type == null)
            {
                return NotFound(new { message = "Type de compte non trouvé" });
            }
            return Ok(type);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors de la récupération du type de compte {id}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }
}
