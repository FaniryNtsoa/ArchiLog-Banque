namespace Epargne.Models.DTOs;

/// <summary>
/// DTO pour les opérations (dépôt/retrait)
/// </summary>
public class OperationEpargneDTO
{
    public int? IdOperation { get; set; }
    public int IdCompte { get; set; }
    public string TypeOperation { get; set; } = string.Empty;
    public decimal Montant { get; set; }
    public decimal SoldeAvant { get; set; }
    public decimal SoldeApres { get; set; }
    public string? Description { get; set; }
    public string? ReferenceOperation { get; set; }
    public DateTime DateOperation { get; set; }
}
