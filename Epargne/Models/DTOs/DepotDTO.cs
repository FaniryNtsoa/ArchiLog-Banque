namespace Epargne.Models.DTOs;

/// <summary>
/// DTO pour une demande de dépôt
/// </summary>
public class DepotDTO
{
    public int IdCompte { get; set; }
    public decimal Montant { get; set; }
    public string? Description { get; set; }
}
