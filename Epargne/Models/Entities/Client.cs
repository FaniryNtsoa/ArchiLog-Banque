using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Epargne.Models.Enums;

namespace Epargne.Models.Entities;

/// <summary>
/// Entité représentant un client de la banque
/// Structure identique aux modules Prêt et Situation Bancaire
/// </summary>
[Table("client")]
public class Client
{
    [Key]
    [Column("id_client")]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long IdClient { get; set; }

    [Column("numero_client")]
    [Required]
    [MaxLength(20)]
    public string NumeroClient { get; set; } = string.Empty;

    [Column("nom")]
    [Required]
    [MaxLength(100)]
    public string Nom { get; set; } = string.Empty;

    [Column("prenom")]
    [Required]
    [MaxLength(100)]
    public string Prenom { get; set; } = string.Empty;

    [Column("date_naissance")]
    [Required]
    public DateTime DateNaissance { get; set; }

    [Column("num_cin")]
    [Required]
    [MaxLength(20)]
    public string NumCin { get; set; } = string.Empty;

    [Column("email")]
    [Required]
    [MaxLength(150)]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Column("telephone")]
    [MaxLength(20)]
    public string? Telephone { get; set; }

    [Column("adresse")]
    [MaxLength(255)]
    public string? Adresse { get; set; }

    [Column("code_postal")]
    [MaxLength(10)]
    public string? CodePostal { get; set; }

    [Column("ville")]
    [MaxLength(100)]
    public string? Ville { get; set; }

    [Column("profession")]
    [MaxLength(100)]
    public string? Profession { get; set; }

    [Column("revenu_mensuel", TypeName = "decimal(15,2)")]
    public decimal? RevenuMensuel { get; set; }

    [Column("solde_initial", TypeName = "decimal(15,2)")]
    [Required]
    public decimal SoldeInitial { get; set; } = 0;

    [Column("situation_familiale")]
    public SituationFamiliale? SituationFamiliale { get; set; }

    [Column("mot_de_passe")]
    [Required]
    [MaxLength(255)]
    public string MotDePasse { get; set; } = string.Empty;

    [Column("statut")]
    [Required]
    public StatutClient Statut { get; set; } = StatutClient.ACTIF;

    [Column("date_creation")]
    public DateTime DateCreation { get; set; } = DateTime.UtcNow;

    [Column("date_modification")]
    public DateTime DateModification { get; set; } = DateTime.UtcNow;

    // Relations
    public virtual ICollection<CompteEpargne> ComptesEpargne { get; set; } = new List<CompteEpargne>();
}
