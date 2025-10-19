namespace Epargne.Models.DTOs;

/// <summary>
/// DTO pour les informations de compte épargne
/// </summary>
public class CompteEpargneDTO
{
    public int? IdCompte { get; set; }
    public long IdClient { get; set; }
    public int IdTypeCompte { get; set; }
    public string? NumeroCompte { get; set; }
    public string? LibelleCompte { get; set; }
    public decimal Solde { get; set; }
    public decimal SoldeDisponible { get; set; }
    public decimal SoldeMinHistorique { get; set; }
    public DateOnly DateOuverture { get; set; }
    public DateOnly? DateDernierCalculInteret { get; set; }
    public DateOnly? DateDerniereOperation { get; set; }
    public string Statut { get; set; } = string.Empty;
    public string? MotifFermeture { get; set; }
    public DateOnly? DateFermeture { get; set; }

    // Informations supplémentaires
    public TypeCompteEpargneDTO? TypeCompte { get; set; }
    public ClientDTO? Client { get; set; }
}
