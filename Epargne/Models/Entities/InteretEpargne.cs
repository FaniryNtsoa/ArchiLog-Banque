using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Epargne.Models.Enums;

namespace Epargne.Models.Entities;

/// <summary>
/// Calcul et historique des intérêts
/// </summary>
[Table("interet_epargne")]
public class InteretEpargne
{
    [Key]
    [Column("id_interet")]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int IdInteret { get; set; }

    [Column("id_compte")]
    [Required]
    public int IdCompte { get; set; }

    [Column("periode_debut")]
    [Required]
    public DateOnly PeriodeDebut { get; set; }

    [Column("periode_fin")]
    [Required]
    public DateOnly PeriodeFin { get; set; }

    [Column("solde_moyen_periode", TypeName = "decimal(15,2)")]
    [Required]
    public decimal SoldeMoyenPeriode { get; set; }

    [Column("taux_interet_applique", TypeName = "decimal(5,3)")]
    [Required]
    public decimal TauxInteretApplique { get; set; }

    [Column("jours_periode")]
    [Required]
    public int JoursPeriode { get; set; }

    [Column("interet_couru", TypeName = "decimal(15,2)")]
    [Required]
    public decimal InteretCouru { get; set; }

    [Column("interet_net", TypeName = "decimal(15,2)")]
    [Required]
    public decimal InteretNet { get; set; }

    [Column("date_calcul")]
    public DateTime DateCalcul { get; set; } = DateTime.UtcNow;

    [Column("date_credit_interet")]
    public DateOnly? DateCreditInteret { get; set; }

    [Column("statut")]
    [Required]
    public InteretStatut Statut { get; set; } = InteretStatut.CALCULE;

    // Relations
    [ForeignKey("IdCompte")]
    public virtual CompteEpargne Compte { get; set; } = null!;
}
