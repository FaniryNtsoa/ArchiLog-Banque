namespace Epargne.Utilities;

/// <summary>
/// Utilitaire pour générer des numéros uniques
/// </summary>
public static class NumeroGenerator
{
    /// <summary>
    /// Génère un numéro de client unique
    /// Format: CLI + timestamp + 4 chiffres aléatoires
    /// </summary>
    public static string GenererNumeroClient()
    {
        long timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        int random = Random.Shared.Next(0, 10000);
        return $"CLI{timestamp}{random:D4}";
    }

    /// <summary>
    /// Génère un numéro de compte épargne unique
    /// Format: EPA + timestamp + 4 chiffres aléatoires
    /// </summary>
    public static string GenererNumeroCompteEpargne()
    {
        long timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        int random = Random.Shared.Next(0, 10000);
        return $"EPA{timestamp}{random:D4}";
    }

    /// <summary>
    /// Génère une référence d'opération unique
    /// Format: OPE + timestamp + 4 chiffres aléatoires
    /// </summary>
    public static string GenererReferenceOperation()
    {
        long timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        int random = Random.Shared.Next(0, 10000);
        return $"OPE{timestamp}{random:D4}";
    }
}
