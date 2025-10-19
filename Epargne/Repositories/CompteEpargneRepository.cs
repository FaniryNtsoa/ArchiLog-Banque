using Microsoft.EntityFrameworkCore;
using Epargne.Data;
using Epargne.Models.Entities;

namespace Epargne.Repositories;

/// <summary>
/// Repository pour les comptes épargne
/// </summary>
public interface ICompteEpargneRepository
{
    Task<CompteEpargne?> GetByIdAsync(int id);
    Task<CompteEpargne?> GetByNumeroCompteAsync(string numeroCompte);
    Task<IEnumerable<CompteEpargne>> GetByClientIdAsync(long clientId);
    Task<IEnumerable<CompteEpargne>> GetAllAsync();
    Task<CompteEpargne> CreateAsync(CompteEpargne compte);
    Task<CompteEpargne> UpdateAsync(CompteEpargne compte);
    Task DeleteAsync(int id);
}

public class CompteEpargneRepository : ICompteEpargneRepository
{
    private readonly EpargneDbContext _context;

    public CompteEpargneRepository(EpargneDbContext context)
    {
        _context = context;
    }

    public async Task<CompteEpargne?> GetByIdAsync(int id)
    {
        return await _context.ComptesEpargne
            .Include(c => c.Client)
            .Include(c => c.TypeCompte)
            .Include(c => c.Operations.OrderByDescending(o => o.DateOperation).Take(10))
            .FirstOrDefaultAsync(c => c.IdCompte == id);
    }

    public async Task<CompteEpargne?> GetByNumeroCompteAsync(string numeroCompte)
    {
        return await _context.ComptesEpargne
            .Include(c => c.Client)
            .Include(c => c.TypeCompte)
            .FirstOrDefaultAsync(c => c.NumeroCompte == numeroCompte);
    }

    public async Task<IEnumerable<CompteEpargne>> GetByClientIdAsync(long clientId)
    {
        return await _context.ComptesEpargne
            .Include(c => c.TypeCompte)
            .Where(c => c.IdClient == clientId)
            .ToListAsync();
    }

    public async Task<IEnumerable<CompteEpargne>> GetAllAsync()
    {
        return await _context.ComptesEpargne
            .Include(c => c.Client)
            .Include(c => c.TypeCompte)
            .ToListAsync();
    }

    public async Task<CompteEpargne> CreateAsync(CompteEpargne compte)
    {
        _context.ComptesEpargne.Add(compte);
        await _context.SaveChangesAsync();
        return compte;
    }

    public async Task<CompteEpargne> UpdateAsync(CompteEpargne compte)
    {
        compte.DateModification = DateTime.UtcNow;
        _context.ComptesEpargne.Update(compte);
        await _context.SaveChangesAsync();
        return compte;
    }

    public async Task DeleteAsync(int id)
    {
        var compte = await _context.ComptesEpargne.FindAsync(id);
        if (compte != null)
        {
            _context.ComptesEpargne.Remove(compte);
            await _context.SaveChangesAsync();
        }
    }
}
