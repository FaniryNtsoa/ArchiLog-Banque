using Microsoft.EntityFrameworkCore;
using Epargne.Models.Entities;
using Epargne.Models.Enums;

namespace Epargne.Data;

/// <summary>
/// Contexte de base de données pour le module Épargne
/// </summary>
public class EpargneDbContext : DbContext
{
    public EpargneDbContext(DbContextOptions<EpargneDbContext> options) : base(options)
    {
    }

    // DbSets
    public DbSet<Client> Clients { get; set; }
    public DbSet<TypeCompteEpargne> TypesCompteEpargne { get; set; }
    public DbSet<CompteEpargne> ComptesEpargne { get; set; }
    public DbSet<OperationEpargne> OperationsEpargne { get; set; }
    public DbSet<InteretEpargne> InteretsEpargne { get; set; }
    public DbSet<RestrictionEpargne> RestrictionsEpargne { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // Configuration de l'entité Client
        modelBuilder.Entity<Client>(entity =>
        {
            entity.HasKey(e => e.IdClient);
            entity.HasIndex(e => e.NumeroClient).IsUnique();
            entity.HasIndex(e => e.Email).IsUnique();
            entity.HasIndex(e => e.NumCin).IsUnique();

            entity.Property(e => e.Statut)
                .HasConversion<string>();

            entity.Property(e => e.SituationFamiliale)
                .HasConversion<string>();

            // Contrainte CHECK pour Statut
            entity.ToTable(t => t.HasCheckConstraint(
                "CK_Client_Statut", 
                "statut IN ('ACTIF', 'INACTIF', 'SUSPENDU', 'FERME')"
            ));

            // Contrainte CHECK pour SituationFamiliale
            entity.ToTable(t => t.HasCheckConstraint(
                "CK_Client_SituationFamiliale", 
                "situation_familiale IS NULL OR situation_familiale IN ('CELIBATAIRE', 'MARIE', 'DIVORCE', 'VEUF')"
            ));
        });

        // Configuration de l'entité TypeCompteEpargne
        modelBuilder.Entity<TypeCompteEpargne>(entity =>
        {
            entity.HasKey(e => e.IdTypeCompte);
            entity.HasIndex(e => e.CodeType).IsUnique();

            entity.Property(e => e.PeriodiciteCalculInteret)
                .HasConversion<string>();

            // Contrainte CHECK pour PeriodiciteCalculInteret
            entity.ToTable(t => t.HasCheckConstraint(
                "CK_TypeCompteEpargne_Periodicite", 
                "periodicite_calcul_interet IN ('QUOTIDIEN', 'MENSUEL', 'TRIMESTRIEL', 'ANNUEL')"
            ));
        });

        // Configuration de l'entité CompteEpargne
        modelBuilder.Entity<CompteEpargne>(entity =>
        {
            entity.HasKey(e => e.IdCompte);
            entity.HasIndex(e => e.NumeroCompte).IsUnique();
            entity.HasIndex(e => e.IdClient);
            entity.HasIndex(e => e.IdTypeCompte);

            entity.Property(e => e.Statut)
                .HasConversion<string>();

            entity.HasOne(e => e.Client)
                .WithMany(c => c.ComptesEpargne)
                .HasForeignKey(e => e.IdClient)
                .OnDelete(DeleteBehavior.Restrict);

            entity.HasOne(e => e.TypeCompte)
                .WithMany(t => t.ComptesEpargne)
                .HasForeignKey(e => e.IdTypeCompte)
                .OnDelete(DeleteBehavior.Restrict);

            // Contrainte CHECK pour Statut
            entity.ToTable(t => t.HasCheckConstraint(
                "CK_CompteEpargne_Statut", 
                "statut IN ('ACTIF', 'INACTIF', 'CLOTURE', 'BLOQUE')"
            ));
        });

        // Configuration de l'entité OperationEpargne
        modelBuilder.Entity<OperationEpargne>(entity =>
        {
            entity.HasKey(e => e.IdOperation);
            entity.HasIndex(e => e.IdCompte);
            entity.HasIndex(e => e.DateOperation);

            entity.Property(e => e.TypeOperation)
                .HasConversion<string>();

            entity.HasOne(e => e.Compte)
                .WithMany(c => c.Operations)
                .HasForeignKey(e => e.IdCompte)
                .OnDelete(DeleteBehavior.Restrict);

            // Contrainte CHECK pour TypeOperation
            entity.ToTable(t => t.HasCheckConstraint(
                "CK_OperationEpargne_TypeOperation", 
                "type_operation IN ('DEPOT', 'RETRAIT', 'VIREMENT_EMIS', 'VIREMENT_RECU', 'INTERETS', 'FRAIS', 'OUVERTURE', 'CLOTURE')"
            ));
        });

        // Configuration de l'entité InteretEpargne
        modelBuilder.Entity<InteretEpargne>(entity =>
        {
            entity.HasKey(e => e.IdInteret);
            entity.HasIndex(e => e.IdCompte);
            entity.HasIndex(e => new { e.PeriodeDebut, e.PeriodeFin });

            entity.Property(e => e.Statut)
                .HasConversion<string>();

            entity.HasOne(e => e.Compte)
                .WithMany(c => c.Interets)
                .HasForeignKey(e => e.IdCompte)
                .OnDelete(DeleteBehavior.Restrict);

            // Contrainte CHECK pour Statut
            entity.ToTable(t => t.HasCheckConstraint(
                "CK_InteretEpargne_Statut", 
                "statut IN ('EN_ATTENTE', 'CAPITALISE', 'ANNULE')"
            ));
        });

        // Configuration de l'entité RestrictionEpargne
        modelBuilder.Entity<RestrictionEpargne>(entity =>
        {
            entity.HasKey(e => e.IdRestriction);

            entity.HasOne(e => e.TypeCompte)
                .WithMany(t => t.Restrictions)
                .HasForeignKey(e => e.IdTypeCompte)
                .OnDelete(DeleteBehavior.Cascade);
        });

        // Données de démarrage pour les types de comptes
        SeedData(modelBuilder);
    }

    private void SeedData(ModelBuilder modelBuilder)
    {
        // Date fixe pour les seed data
        var seedDate = new DateTime(2025, 1, 1, 0, 0, 0, DateTimeKind.Utc);
        
        modelBuilder.Entity<TypeCompteEpargne>().HasData(
            new TypeCompteEpargne
            {
                IdTypeCompte = 1,
                CodeType = "LIVRET_A",
                Libelle = "Livret A",
                Description = "Compte d'épargne classique avec taux garanti",
                TauxInteretAnnuel = 3.00m,
                DepotInitialMin = 10.00m,
                SoldeMinObligatoire = 10.00m,
                PlafondDepot = 22950.00m,
                RetraitMaxPourcentage = 100.00m,
                FraisTenueCompte = 0.00m,
                PeriodiciteCalculInteret = PeriodiciteCalculInteret.QUOTIDIEN,
                Actif = true,
                DateCreation = seedDate
            },
            new TypeCompteEpargne
            {
                IdTypeCompte = 2,
                CodeType = "CEL",
                Libelle = "Compte Épargne Logement",
                Description = "Compte d'épargne réglementé pour financement immobilier",
                TauxInteretAnnuel = 2.00m,
                DepotInitialMin = 300.00m,
                SoldeMinObligatoire = 300.00m,
                PlafondDepot = 15300.00m,
                RetraitMaxPourcentage = 50.00m,
                FraisTenueCompte = 0.00m,
                PeriodiciteCalculInteret = PeriodiciteCalculInteret.MENSUEL,
                Actif = true,
                DateCreation = seedDate
            },
            new TypeCompteEpargne
            {
                IdTypeCompte = 3,
                CodeType = "LDD",
                Libelle = "Livret de Développement Durable",
                Description = "Livret d'épargne fiscalement avantageux",
                TauxInteretAnnuel = 3.00m,
                DepotInitialMin = 15.00m,
                SoldeMinObligatoire = 15.00m,
                PlafondDepot = 12000.00m,
                RetraitMaxPourcentage = 100.00m,
                FraisTenueCompte = 0.00m,
                PeriodiciteCalculInteret = PeriodiciteCalculInteret.QUOTIDIEN,
                Actif = true,
                DateCreation = seedDate
            },
            new TypeCompteEpargne
            {
                IdTypeCompte = 4,
                CodeType = "PEL",
                Libelle = "Plan Épargne Logement",
                Description = "Plan d'épargne à terme pour projet immobilier",
                TauxInteretAnnuel = 2.25m,
                DepotInitialMin = 225.00m,
                SoldeMinObligatoire = 225.00m,
                PlafondDepot = 61200.00m,
                RetraitMaxPourcentage = 0.00m, // Pas de retrait partiel
                FraisTenueCompte = 0.00m,
                PeriodiciteCalculInteret = PeriodiciteCalculInteret.TRIMESTRIEL,
                Actif = true,
                DateCreation = seedDate
            }
        );
    }
}
