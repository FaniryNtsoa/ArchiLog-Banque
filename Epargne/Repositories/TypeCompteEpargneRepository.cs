using Microsoft.EntityFrameworkCore;
using Epargne.Data;
using Epargne.Models.Entities;

namespace Epargne.Repositories;

/// <summary>
/// Repository pour les types de comptes épargne
/// </summary>
public interface ITypeCompteEpargneRepository
{
    Task<TypeCompteEpargne?> GetByIdAsync(int id);
    Task<TypeCompteEpargne?> GetByCodeAsync(string code);
    Task<IEnumerable<TypeCompteEpargne>> GetAllAsync();
    Task<IEnumerable<TypeCompteEpargne>> GetAllActifsAsync();
}

public class TypeCompteEpargneRepository : ITypeCompteEpargneRepository
{
    private readonly EpargneDbContext _context;

    public TypeCompteEpargneRepository(EpargneDbContext context)
    {
        _context = context;
    }

    public async Task<TypeCompteEpargne?> GetByIdAsync(int id)
    {
        return await _context.TypesCompteEpargne
            .Include(t => t.Restrictions)
            .FirstOrDefaultAsync(t => t.IdTypeCompte == id);
    }

    public async Task<TypeCompteEpargne?> GetByCodeAsync(string code)
    {
        return await _context.TypesCompteEpargne
            .Include(t => t.Restrictions)
            .FirstOrDefaultAsync(t => t.CodeType == code);
    }

    public async Task<IEnumerable<TypeCompteEpargne>> GetAllAsync()
    {
        return await _context.TypesCompteEpargne.ToListAsync();
    }

    public async Task<IEnumerable<TypeCompteEpargne>> GetAllActifsAsync()
    {
        return await _context.TypesCompteEpargne
            .Where(t => t.Actif)
            .ToListAsync();
    }
}
