using Epargne.Models.DTOs;
using Epargne.Models.Entities;
using Epargne.Models.Enums;
using Epargne.Repositories;
using Epargne.Utilities;

namespace Epargne.Services;

/// <summary>
/// Service pour la gestion des clients
/// </summary>
public interface IClientService
{
    Task<ClientDTO?> GetClientByIdAsync(long id);
    Task<ClientDTO?> GetClientByEmailAsync(string email);
    Task<ClientDTO> CreateClientAsync(ClientDTO clientDTO);
    Task<ClientDTO> UpdateClientAsync(ClientDTO clientDTO);
    // Note: L'authentification client a été supprimée - toutes les opérations sont gérées par l'administrateur
    Task<IEnumerable<ClientDTO>> GetAllClientsAsync();
}

public class ClientService : IClientService
{
    private readonly IClientRepository _clientRepository;
    private readonly ILogger<ClientService> _logger;

    public ClientService(IClientRepository clientRepository, ILogger<ClientService> logger)
    {
        _clientRepository = clientRepository;
        _logger = logger;
    }

    public async Task<ClientDTO?> GetClientByIdAsync(long id)
    {
        var client = await _clientRepository.GetByIdAsync(id);
        return client != null ? MapToDTO(client) : null;
    }

    public async Task<ClientDTO?> GetClientByEmailAsync(string email)
    {
        var client = await _clientRepository.GetByEmailAsync(email);
        return client != null ? MapToDTO(client) : null;
    }

    public async Task<ClientDTO> CreateClientAsync(ClientDTO clientDTO)
    {
        _logger.LogInformation($"Création d'un nouveau client : {clientDTO.Email}");

        // Vérifications métier
        if (await _clientRepository.ExistsByEmailAsync(clientDTO.Email))
        {
            throw new InvalidOperationException("Un client avec cet email existe déjà");
        }

        if (await _clientRepository.ExistsByNumCinAsync(clientDTO.NumCin))
        {
            throw new InvalidOperationException("Un client avec ce numéro CIN existe déjà");
        }

        // Créer l'entité client
        var client = new Client
        {
            NumeroClient = NumeroGenerator.GenererNumeroClient(),
            Nom = clientDTO.Nom,
            Prenom = clientDTO.Prenom,
            DateNaissance = EnsureUtc(clientDTO.DateNaissance),
            NumCin = clientDTO.NumCin,
            Email = clientDTO.Email,
            Telephone = clientDTO.Telephone,
            Adresse = clientDTO.Adresse,
            CodePostal = clientDTO.CodePostal,
            Ville = clientDTO.Ville,
            Profession = clientDTO.Profession,
            RevenuMensuel = clientDTO.RevenuMensuel,
            SoldeInitial = clientDTO.SoldeInitial,
            SituationFamiliale = Enum.TryParse<SituationFamiliale>(clientDTO.SituationFamiliale, out var sf) ? sf : null,
            MotDePasse = PasswordHasher.HashPassword(clientDTO.MotDePasse ?? ""),
            Statut = StatutClient.ACTIF,
            DateCreation = DateTime.UtcNow,
            DateModification = DateTime.UtcNow
        };

        var createdClient = await _clientRepository.CreateAsync(client);
        _logger.LogInformation($"Client créé avec succès : {createdClient.NumeroClient}");

        return MapToDTO(createdClient);
    }

    public async Task<ClientDTO> UpdateClientAsync(ClientDTO clientDTO)
    {
        if (clientDTO.IdClient == null)
        {
            throw new ArgumentException("L'ID du client est requis pour la mise à jour");
        }

        var existingClient = await _clientRepository.GetByIdAsync(clientDTO.IdClient.Value);
        if (existingClient == null)
        {
            throw new InvalidOperationException("Client non trouvé");
        }

        // Vérifier que l'email n'est pas utilisé par un autre client
        if (existingClient.Email != clientDTO.Email && await _clientRepository.ExistsByEmailAsync(clientDTO.Email))
        {
            throw new InvalidOperationException("Un autre client utilise déjà cet email");
        }

        // Mettre à jour les propriétés
        existingClient.Nom = clientDTO.Nom;
        existingClient.Prenom = clientDTO.Prenom;
        existingClient.DateNaissance = EnsureUtc(clientDTO.DateNaissance);
        existingClient.Email = clientDTO.Email;
        existingClient.Telephone = clientDTO.Telephone;
        existingClient.Adresse = clientDTO.Adresse;
        existingClient.CodePostal = clientDTO.CodePostal;
        existingClient.Ville = clientDTO.Ville;
        existingClient.Profession = clientDTO.Profession;
        existingClient.RevenuMensuel = clientDTO.RevenuMensuel;
        
        if (!string.IsNullOrEmpty(clientDTO.SituationFamiliale))
        {
            existingClient.SituationFamiliale = Enum.Parse<SituationFamiliale>(clientDTO.SituationFamiliale);
        }

        // Mettre à jour le mot de passe si fourni
        if (!string.IsNullOrEmpty(clientDTO.MotDePasse))
        {
            existingClient.MotDePasse = PasswordHasher.HashPassword(clientDTO.MotDePasse);
        }

        var updatedClient = await _clientRepository.UpdateAsync(existingClient);
        return MapToDTO(updatedClient);
    }

    public async Task<IEnumerable<ClientDTO>> GetAllClientsAsync()
    {
        var clients = await _clientRepository.GetAllAsync();
        return clients.Select(MapToDTO);
    }

    // Note: La méthode AuthenticateAsync a été supprimée car l'authentification se fait maintenant via le centralisateur

    private static ClientDTO MapToDTO(Client client)
    {
        return new ClientDTO
        {
            IdClient = client.IdClient,
            NumeroClient = client.NumeroClient,
            Nom = client.Nom,
            Prenom = client.Prenom,
            DateNaissance = client.DateNaissance,
            NumCin = client.NumCin,
            Email = client.Email,
            Telephone = client.Telephone,
            Adresse = client.Adresse,
            CodePostal = client.CodePostal,
            Ville = client.Ville,
            Profession = client.Profession,
            RevenuMensuel = client.RevenuMensuel,
            SoldeInitial = client.SoldeInitial,
            SituationFamiliale = client.SituationFamiliale?.ToString(),
            Statut = client.Statut.ToString(),
            DateCreation = client.DateCreation,
            DateModification = client.DateModification
        };
    }

    /// <summary>
    /// Convertit un DateTime en UTC si nécessaire
    /// PostgreSQL exige que tous les DateTime soient en UTC
    /// </summary>
    private static DateTime EnsureUtc(DateTime dateTime)
    {
        if (dateTime.Kind == DateTimeKind.Utc)
        {
            return dateTime;
        }
        
        if (dateTime.Kind == DateTimeKind.Unspecified)
        {
            // Considérer les dates non spécifiées comme UTC
            return DateTime.SpecifyKind(dateTime, DateTimeKind.Utc);
        }
        
        // Convertir les dates locales en UTC
        return dateTime.ToUniversalTime();
    }
}
