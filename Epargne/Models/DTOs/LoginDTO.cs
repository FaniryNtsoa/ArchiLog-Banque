namespace Epargne.Models.DTOs;

/// <summary>
/// DTO pour l'authentification client
/// </summary>
public class LoginDTO
{
    public string Email { get; set; } = string.Empty;
    public string MotDePasse { get; set; } = string.Empty;
}
