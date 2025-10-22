namespace Epargne.Models.DTOs;

/// <summary>
/// DTO pour une demande de retrait effectuée par un administrateur
/// </summary>
public class RetraitDTO
{
    public int IdCompte { get; set; }
    public decimal Montant { get; set; }
    public string? Description { get; set; }
    
    /// <summary>
    /// ID de l'administrateur qui effectue l'opération (nullable)
    /// </summary>
    public long? IdAdministrateur { get; set; }
    
    /// <summary>
    /// Date de l'opération (optionnelle, si null utilise DateTime.UtcNow)
    /// Permet de spécifier une date pour les tests
    /// </summary>
    public DateTime? DateOperation { get; set; }
}
