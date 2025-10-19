namespace Epargne.Models.DTOs;

/// <summary>
/// DTO pour les informations de type de compte épargne
/// </summary>
public class TypeCompteEpargneDTO
{
    public int? IdTypeCompte { get; set; }
    public string CodeType { get; set; } = string.Empty;
    public string Libelle { get; set; } = string.Empty;
    public string? Description { get; set; }
    public decimal TauxInteretAnnuel { get; set; }
    public decimal DepotInitialMin { get; set; }
    public decimal SoldeMinObligatoire { get; set; }
    public decimal PlafondDepot { get; set; }
    public decimal RetraitMaxPourcentage { get; set; }
    public decimal FraisTenueCompte { get; set; }
    public string PeriodiciteCalculInteret { get; set; } = string.Empty;
    public bool Actif { get; set; }
}
