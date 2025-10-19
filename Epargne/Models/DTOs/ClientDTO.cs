namespace Epargne.Models.DTOs;

/// <summary>
/// DTO pour les informations client
/// Structure identique aux autres modules
/// </summary>
public class ClientDTO
{
    public long? IdClient { get; set; }
    public string? NumeroClient { get; set; }
    public string Nom { get; set; } = string.Empty;
    public string Prenom { get; set; } = string.Empty;
    public DateTime DateNaissance { get; set; }
    public string NumCin { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string? Telephone { get; set; }
    public string? Adresse { get; set; }
    public string? CodePostal { get; set; }
    public string? Ville { get; set; }
    public string? Profession { get; set; }
    public decimal? RevenuMensuel { get; set; }
    public decimal SoldeInitial { get; set; }
    public string? SituationFamiliale { get; set; }
    public string? MotDePasse { get; set; } // Pour inscription/modification
    public string? Statut { get; set; }
    public DateTime? DateCreation { get; set; }
    public DateTime? DateModification { get; set; }
}
