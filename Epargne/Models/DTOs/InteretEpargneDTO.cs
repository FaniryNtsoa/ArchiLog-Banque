namespace Epargne.Models.DTOs;

/// <summary>
/// DTO pour les informations d'intérêt
/// </summary>
public class InteretEpargneDTO
{
    public int? IdInteret { get; set; }
    public int IdCompte { get; set; }
    public DateOnly PeriodeDebut { get; set; }
    public DateOnly PeriodeFin { get; set; }
    public decimal SoldeMoyenPeriode { get; set; }
    public decimal TauxInteretApplique { get; set; }
    public int JoursPeriode { get; set; }
    public decimal InteretCouru { get; set; }
    public decimal InteretNet { get; set; }
    public DateTime DateCalcul { get; set; }
    public DateOnly? DateCreditInteret { get; set; }
    public string Statut { get; set; } = string.Empty;
}
