using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Epargne.Models.Entities;

/// <summary>
/// Restrictions et plafonds pour un type de compte
/// </summary>
[Table("restriction_epargne")]
public class RestrictionEpargne
{
    [Key]
    [Column("id_restriction")]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int IdRestriction { get; set; }

    [Column("id_type_compte")]
    [Required]
    public int IdTypeCompte { get; set; }

    [Column("type_restriction")]
    [Required]
    [MaxLength(50)]
    public string TypeRestriction { get; set; } = string.Empty;

    [Column("valeur", TypeName = "decimal(15,2)")]
    [Required]
    public decimal Valeur { get; set; }

    [Column("unite")]
    [MaxLength(20)]
    public string? Unite { get; set; }

    [Column("description")]
    public string? Description { get; set; }

    [Column("date_debut")]
    [Required]
    public DateOnly DateDebut { get; set; }

    [Column("date_fin")]
    public DateOnly? DateFin { get; set; }

    [Column("date_creation")]
    public DateTime DateCreation { get; set; } = DateTime.UtcNow;

    // Relations
    [ForeignKey("IdTypeCompte")]
    public virtual TypeCompteEpargne TypeCompte { get; set; } = null!;
}
