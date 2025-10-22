using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Epargne.Models.Enums;

namespace Epargne.Models.Entities;

/// <summary>
/// Opération sur un compte épargne (dépôt, retrait, etc.)
/// </summary>
[Table("operation_epargne")]
public class OperationEpargne
{
    [Key]
    [Column("id_operation")]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int IdOperation { get; set; }

    [Column("id_compte")]
    [Required]
    public int IdCompte { get; set; }

    [Column("type_operation")]
    [Required]
    public OperationType TypeOperation { get; set; }

    [Column("montant", TypeName = "decimal(15,2)")]
    [Required]
    public decimal Montant { get; set; }

    [Column("solde_avant", TypeName = "decimal(15,2)")]
    [Required]
    public decimal SoldeAvant { get; set; }

    [Column("solde_apres", TypeName = "decimal(15,2)")]
    [Required]
    public decimal SoldeApres { get; set; }

    [Column("description")]
    [MaxLength(255)]
    public string? Description { get; set; }

    [Column("reference_operation")]
    [MaxLength(100)]
    public string? ReferenceOperation { get; set; }

    [Column("date_operation")]
    public DateTime DateOperation { get; set; } = DateTime.UtcNow;

    /// <summary>
    /// ID de l'administrateur qui a effectué l'opération (nullable)
    /// </summary>
    [Column("id_administrateur")]
    public long? IdAdministrateur { get; set; }

    // Relations
    [ForeignKey("IdCompte")]
    public virtual CompteEpargne Compte { get; set; } = null!;
}
