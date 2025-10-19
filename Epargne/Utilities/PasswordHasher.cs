using System.Security.Cryptography;
using System.Text;

namespace Epargne.Utilities;

/// <summary>
/// Utilitaire pour le hachage et la vérification des mots de passe
/// Compatible avec l'implémentation Java (SHA-256)
/// </summary>
public static class PasswordHasher
{
    /// <summary>
    /// Hash un mot de passe en utilisant SHA-256
    /// </summary>
    public static string HashPassword(string password)
    {
        using var sha256 = SHA256.Create();
        var bytes = Encoding.UTF8.GetBytes(password);
        var hash = sha256.ComputeHash(bytes);
        
        var hexString = new StringBuilder();
        foreach (byte b in hash)
        {
            hexString.Append(b.ToString("x2"));
        }
        
        return hexString.ToString();
    }

    /// <summary>
    /// Vérifie si un mot de passe correspond au hash
    /// </summary>
    public static bool VerifyPassword(string password, string hash)
    {
        var passwordHash = HashPassword(password);
        return passwordHash.Equals(hash, StringComparison.OrdinalIgnoreCase);
    }
}
