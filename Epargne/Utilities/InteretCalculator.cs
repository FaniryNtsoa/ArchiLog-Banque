namespace Epargne.Utilities;

/// <summary>
/// Utilitaire pour les calculs d'intérêts sur comptes épargne
/// </summary>
public static class InteretCalculator
{
    /// <summary>
    /// Calcule les intérêts selon la formule :
    /// Intérêts = Solde moyen × Taux annuel × (Nombre de jours / 365)
    /// </summary>
    /// <param name="soldeMoyen">Solde moyen de la période</param>
    /// <param name="tauxAnnuel">Taux d'intérêt annuel (en décimal, ex: 0.03 pour 3%)</param>
    /// <param name="nombreJours">Nombre de jours de la période</param>
    /// <returns>Montant des intérêts calculés</returns>
    public static decimal CalculerInterets(decimal soldeMoyen, decimal tauxAnnuel, int nombreJours)
    {
        if (soldeMoyen < 0)
            throw new ArgumentException("Le solde moyen ne peut pas être négatif", nameof(soldeMoyen));
        
        if (tauxAnnuel < 0)
            throw new ArgumentException("Le taux d'intérêt ne peut pas être négatif", nameof(tauxAnnuel));
        
        if (nombreJours <= 0)
            throw new ArgumentException("Le nombre de jours doit être positif", nameof(nombreJours));

        // Formule : Intérêts = Solde moyen × Taux annuel × (Nombre de jours / 365)
        decimal interets = soldeMoyen * (tauxAnnuel / 100) * (nombreJours / 365.0m);
        
        // Arrondir à 2 décimales
        return Math.Round(interets, 2);
    }

    /// <summary>
    /// Calcule le solde moyen sur une période
    /// </summary>
    /// <param name="soldes">Liste des soldes quotidiens</param>
    /// <returns>Solde moyen</returns>
    public static decimal CalculerSoldeMoyen(List<decimal> soldes)
    {
        if (soldes == null || soldes.Count == 0)
            throw new ArgumentException("La liste des soldes ne peut pas être vide", nameof(soldes));

        return Math.Round(soldes.Average(), 2);
    }

    /// <summary>
    /// Calcule le solde minimum sur une période
    /// </summary>
    public static decimal CalculerSoldeMinimum(List<decimal> soldes)
    {
        if (soldes == null || soldes.Count == 0)
            throw new ArgumentException("La liste des soldes ne peut pas être vide", nameof(soldes));

        return soldes.Min();
    }

    /// <summary>
    /// Calcule le nombre de jours entre deux dates
    /// </summary>
    public static int CalculerNombreJours(DateOnly dateDebut, DateOnly dateFin)
    {
        if (dateFin < dateDebut)
            throw new ArgumentException("La date de fin doit être postérieure à la date de début");

        return (dateFin.ToDateTime(TimeOnly.MinValue) - dateDebut.ToDateTime(TimeOnly.MinValue)).Days + 1;
    }

    /// <summary>
    /// Détermine la date de prochaine capitalisation selon la périodicité
    /// </summary>
    public static DateOnly CalculerProchaineDateCapitalisation(DateOnly dateReference, string periodicite)
    {
        return periodicite.ToUpper() switch
        {
            "QUOTIDIEN" => dateReference.AddDays(1),
            "MENSUEL" => dateReference.AddMonths(1),
            "TRIMESTRIEL" => dateReference.AddMonths(3),
            _ => throw new ArgumentException($"Périodicité non supportée: {periodicite}")
        };
    }

    /// <summary>
    /// Vérifie si c'est le moment de capitaliser les intérêts
    /// </summary>
    public static bool DoitCapitaliser(DateOnly dateDernierCalcul, DateOnly dateActuelle, string periodicite)
    {
        return periodicite.ToUpper() switch
        {
            "QUOTIDIEN" => dateActuelle.Day == 1, // Premier du mois
            "MENSUEL" => dateActuelle.Day == 1 && dateActuelle.Month != dateDernierCalcul.Month,
            "TRIMESTRIEL" => dateActuelle.Day == 1 && (dateActuelle.Month % 3 == 1) && 
                           ((dateActuelle.Month - dateDernierCalcul.Month) >= 3 || dateActuelle.Year != dateDernierCalcul.Year),
            _ => false
        };
    }
}
