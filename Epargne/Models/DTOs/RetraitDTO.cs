namespace Epargne.Models.DTOs;

/// <summary>
/// DTO pour une demande de retrait
/// </summary>
public class RetraitDTO
{
    public int IdCompte { get; set; }
    public decimal Montant { get; set; }
    public string? Description { get; set; }
}
