namespace Epargne.Models.DTOs;

/// <summary>
/// DTO pour la création d'un compte épargne
/// </summary>
public class CreationCompteEpargneDTO
{
    public long IdClient { get; set; }
    public int IdTypeCompte { get; set; }
    public string? LibelleCompte { get; set; }
    public decimal DepotInitial { get; set; }
}
