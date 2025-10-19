using Epargne.Models.DTOs;
using Epargne.Repositories;

namespace Epargne.Services;

/// <summary>
/// Service pour les types de comptes épargne
/// </summary>
public interface ITypeCompteEpargneService
{
    Task<TypeCompteEpargneDTO?> GetTypeCompteByIdAsync(int id);
    Task<IEnumerable<TypeCompteEpargneDTO>> GetAllTypesComptesAsync();
    Task<IEnumerable<TypeCompteEpargneDTO>> GetTypesComptesActifsAsync();
}

public class TypeCompteEpargneService : ITypeCompteEpargneService
{
    private readonly ITypeCompteEpargneRepository _repository;

    public TypeCompteEpargneService(ITypeCompteEpargneRepository repository)
    {
        _repository = repository;
    }

    public async Task<TypeCompteEpargneDTO?> GetTypeCompteByIdAsync(int id)
    {
        var type = await _repository.GetByIdAsync(id);
        return type != null ? MapToDTO(type) : null;
    }

    public async Task<IEnumerable<TypeCompteEpargneDTO>> GetAllTypesComptesAsync()
    {
        var types = await _repository.GetAllAsync();
        return types.Select(MapToDTO);
    }

    public async Task<IEnumerable<TypeCompteEpargneDTO>> GetTypesComptesActifsAsync()
    {
        var types = await _repository.GetAllActifsAsync();
        return types.Select(MapToDTO);
    }

    private static TypeCompteEpargneDTO MapToDTO(Models.Entities.TypeCompteEpargne type)
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
}
