using Microsoft.AspNetCore.Mvc;
using Epargne.Models.DTOs;
using Epargne.Services;

namespace Epargne.Controllers;

/// <summary>
/// Contrôleur pour la gestion des clients
/// </summary>
[ApiController]
[Route("api/[controller]")]
public class ClientsController : ControllerBase
{
    private readonly IClientService _clientService;
    private readonly ILogger<ClientsController> _logger;

    public ClientsController(IClientService clientService, ILogger<ClientsController> logger)
    {
        _clientService = clientService;
        _logger = logger;
    }

    /// <summary>
    /// Récupère tous les clients
    /// </summary>
    [HttpGet]
    public async Task<ActionResult<IEnumerable<ClientDTO>>> GetAllClients()
    {
        try
        {
            var clients = await _clientService.GetAllClientsAsync();
            return Ok(clients);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erreur lors de la récupération des clients");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Récupère un client par son ID
    /// </summary>
    [HttpGet("{id}")]
    public async Task<ActionResult<ClientDTO>> GetClientById(long id)
    {
        try
        {
            var client = await _clientService.GetClientByIdAsync(id);
            if (client == null)
            {
                return NotFound(new { message = "Client non trouvé" });
            }
            return Ok(client);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors de la récupération du client {id}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Récupère un client par son email
    /// </summary>
    [HttpGet("by-email/{email}")]
    public async Task<ActionResult<ClientDTO>> GetClientByEmail(string email)
    {
        try
        {
            var client = await _clientService.GetClientByEmailAsync(email);
            if (client == null)
            {
                return NotFound(new { message = "Client non trouvé" });
            }
            return Ok(client);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors de la récupération du client par email {email}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Crée un nouveau client (inscription)
    /// </summary>
    [HttpPost]
    public async Task<ActionResult<ClientDTO>> CreateClient([FromBody] ClientDTO clientDTO)
    {
        try
        {
            var createdClient = await _clientService.CreateClientAsync(clientDTO);
            return CreatedAtAction(nameof(GetClientById), new { id = createdClient.IdClient }, createdClient);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erreur lors de la création du client");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Met à jour un client
    /// </summary>
    [HttpPut("{id}")]
    public async Task<ActionResult<ClientDTO>> UpdateClient(long id, [FromBody] ClientDTO clientDTO)
    {
        try
        {
            clientDTO.IdClient = id;
            var updatedClient = await _clientService.UpdateClientAsync(clientDTO);
            return Ok(updatedClient);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"Erreur lors de la mise à jour du client {id}");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }

    /// <summary>
    /// Authentifie un client (connexion)
    /// </summary>
    [HttpPost("login")]
    public async Task<ActionResult<ClientDTO>> Login([FromBody] LoginDTO loginDTO)
    {
        try
        {
            var client = await _clientService.AuthenticateAsync(loginDTO.Email, loginDTO.MotDePasse);
            if (client == null)
            {
                return Unauthorized(new { message = "Email ou mot de passe incorrect" });
            }
            return Ok(client);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erreur lors de l'authentification");
            return StatusCode(500, new { message = "Erreur serveur", details = ex.Message });
        }
    }
}
