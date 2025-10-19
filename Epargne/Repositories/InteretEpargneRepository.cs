using Microsoft.EntityFrameworkCore;
using Epargne.Data;
using Epargne.Models.Entities;

namespace Epargne.Repositories;

/// <summary>
/// Repository pour les intérêts
/// </summary>
public interface IInteretEpargneRepository
{
    Task<InteretEpargne?> GetByIdAsync(int id);
    Task<IEnumerable<InteretEpargne>> GetByCompteIdAsync(int compteId);
    Task<IEnumerable<InteretEpargne>> GetByPeriodeAsync(DateOnly dateDebut, DateOnly dateFin);
    Task<InteretEpargne> CreateAsync(InteretEpargne interet);
    Task<InteretEpargne> UpdateAsync(InteretEpargne interet);
}

public class InteretEpargneRepository : IInteretEpargneRepository
{
    private readonly EpargneDbContext _context;

    public InteretEpargneRepository(EpargneDbContext context)
    {
        _context = context;
    }

    public async Task<InteretEpargne?> GetByIdAsync(int id)
    {
        return await _context.InteretsEpargne
            .Include(i => i.Compte)
            .FirstOrDefaultAsync(i => i.IdInteret == id);
    }

    public async Task<IEnumerable<InteretEpargne>> GetByCompteIdAsync(int compteId)
    {
        return await _context.InteretsEpargne
            .Where(i => i.IdCompte == compteId)
            .OrderByDescending(i => i.PeriodeFin)
            .ToListAsync();
    }

    public async Task<IEnumerable<InteretEpargne>> GetByPeriodeAsync(DateOnly dateDebut, DateOnly dateFin)
    {
        return await _context.InteretsEpargne
            .Where(i => i.PeriodeDebut >= dateDebut && i.PeriodeFin <= dateFin)
            .ToListAsync();
    }

    public async Task<InteretEpargne> CreateAsync(InteretEpargne interet)
    {
        _context.InteretsEpargne.Add(interet);
        await _context.SaveChangesAsync();
        return interet;
    }

    public async Task<InteretEpargne> UpdateAsync(InteretEpargne interet)
    {
        _context.InteretsEpargne.Update(interet);
        await _context.SaveChangesAsync();
        return interet;
    }
}
