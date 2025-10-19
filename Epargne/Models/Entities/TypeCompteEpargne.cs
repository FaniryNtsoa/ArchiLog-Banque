using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Epargne.Models.Enums;

namespace Epargne.Models.Entities;

/// <summary>
/// Type de compte épargne avec ses caractéristiques
/// </summary>
[Table("type_compte_epargne")]
public class TypeCompteEpargne
{
    [Key]
    [Column("id_type_compte")]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int IdTypeCompte { get; set; }

    [Column("code_type")]
    [Required]
    [MaxLength(20)]
    public string CodeType { get; set; } = string.Empty;

    [Column("libelle")]
    [Required]
    [MaxLength(100)]
    public string Libelle { get; set; } = string.Empty;

    [Column("description")]
    public string? Description { get; set; }

    [Column("taux_interet_annuel", TypeName = "decimal(5,3)")]
    [Required]
    public decimal TauxInteretAnnuel { get; set; }

    [Column("depot_initial_min", TypeName = "decimal(15,2)")]
    public decimal DepotInitialMin { get; set; } = 0;

    [Column("solde_min_obligatoire", TypeName = "decimal(15,2)")]
    public decimal SoldeMinObligatoire { get; set; } = 0;

    [Column("plafond_depot", TypeName = "decimal(15,2)")]
    public decimal PlafondDepot { get; set; } = 10000000;

    [Column("retrait_max_pourcentage", TypeName = "decimal(5,2)")]
    public decimal RetraitMaxPourcentage { get; set; } = 50.00m;

    [Column("frais_tenue_compte", TypeName = "decimal(10,2)")]
    public decimal FraisTenueCompte { get; set; } = 0;

    [Column("periodicite_calcul_interet")]
    [MaxLength(20)]
    public PeriodiciteCalculInteret PeriodiciteCalculInteret { get; set; } = PeriodiciteCalculInteret.MENSUEL;

    [Column("actif")]
    public bool Actif { get; set; } = true;

    [Column("date_creation")]
    public DateTime DateCreation { get; set; } = DateTime.UtcNow;

    // Relations
    public virtual ICollection<CompteEpargne> ComptesEpargne { get; set; } = new List<CompteEpargne>();
    public virtual ICollection<RestrictionEpargne> Restrictions { get; set; } = new List<RestrictionEpargne>();
}
