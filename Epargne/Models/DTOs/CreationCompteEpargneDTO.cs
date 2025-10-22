namespace Epargne.Models.DTOs;

/// <summary>
/// DTO pour la création d'un compte épargne par un administrateur
/// </summary>
public class CreationCompteEpargneDTO
{
    public long IdClient { get; set; }
    public int IdTypeCompte { get; set; }
    public string? LibelleCompte { get; set; }
    public decimal DepotInitial { get; set; }
    
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
