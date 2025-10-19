using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Epargne.Models.Enums;

namespace Epargne.Models.Entities;

/// <summary>
/// Compte épargne d'un client
/// </summary>
[Table("compte_epargne")]
public class CompteEpargne
{
    [Key]
    [Column("id_compte")]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int IdCompte { get; set; }

    [Column("id_client")]
    [Required]
    public long IdClient { get; set; }

    [Column("id_type_compte")]
    [Required]
    public int IdTypeCompte { get; set; }

    [Column("numero_compte")]
    [Required]
    [MaxLength(30)]
    public string NumeroCompte { get; set; } = string.Empty;

    [Column("libelle_compte")]
    [MaxLength(100)]
    public string? LibelleCompte { get; set; }

    [Column("solde", TypeName = "decimal(15,2)")]
    [Required]
    public decimal Solde { get; set; } = 0;

    [Column("solde_disponible", TypeName = "decimal(15,2)")]
    [Required]
    public decimal SoldeDisponible { get; set; } = 0;

    [Column("solde_min_historique", TypeName = "decimal(15,2)")]
    public decimal SoldeMinHistorique { get; set; } = 0;

    [Column("date_ouverture")]
    [Required]
    public DateOnly DateOuverture { get; set; } = DateOnly.FromDateTime(DateTime.UtcNow);

    [Column("date_dernier_calcul_interet")]
    public DateOnly? DateDernierCalculInteret { get; set; }

    [Column("date_derniere_operation")]
    public DateOnly? DateDerniereOperation { get; set; }

    [Column("statut")]
    [Required]
    public CompteStatut Statut { get; set; } = CompteStatut.ACTIF;

    [Column("motif_fermeture")]
    public string? MotifFermeture { get; set; }

    [Column("date_fermeture")]
    public DateOnly? DateFermeture { get; set; }

    [Column("date_creation")]
    public DateTime DateCreation { get; set; } = DateTime.UtcNow;

    [Column("date_modification")]
    public DateTime DateModification { get; set; } = DateTime.UtcNow;

    // Relations
    [ForeignKey("IdClient")]
    public virtual Client Client { get; set; } = null!;

    [ForeignKey("IdTypeCompte")]
    public virtual TypeCompteEpargne TypeCompte { get; set; } = null!;

    public virtual ICollection<OperationEpargne> Operations { get; set; } = new List<OperationEpargne>();
    public virtual ICollection<InteretEpargne> Interets { get; set; } = new List<InteretEpargne>();
}
