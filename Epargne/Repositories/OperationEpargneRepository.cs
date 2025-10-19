using Microsoft.EntityFrameworkCore;
using Epargne.Data;
using Epargne.Models.Entities;

namespace Epargne.Repositories;

/// <summary>
/// Repository pour les opérations sur comptes épargne
/// </summary>
public interface IOperationEpargneRepository
{
    Task<OperationEpargne?> GetByIdAsync(int id);
    Task<IEnumerable<OperationEpargne>> GetByCompteIdAsync(int compteId);
    Task<IEnumerable<OperationEpargne>> GetByCompteIdWithPaginationAsync(int compteId, int page, int pageSize);
    Task<OperationEpargne> CreateAsync(OperationEpargne operation);
}

public class OperationEpargneRepository : IOperationEpargneRepository
{
    private readonly EpargneDbContext _context;

    public OperationEpargneRepository(EpargneDbContext context)
    {
        _context = context;
    }

    public async Task<OperationEpargne?> GetByIdAsync(int id)
    {
        return await _context.OperationsEpargne
            .Include(o => o.Compte)
            .FirstOrDefaultAsync(o => o.IdOperation == id);
    }

    public async Task<IEnumerable<OperationEpargne>> GetByCompteIdAsync(int compteId)
    {
        return await _context.OperationsEpargne
            .Where(o => o.IdCompte == compteId)
            .OrderByDescending(o => o.DateOperation)
            .ToListAsync();
    }

    public async Task<IEnumerable<OperationEpargne>> GetByCompteIdWithPaginationAsync(int compteId, int page, int pageSize)
    {
        return await _context.OperationsEpargne
            .Where(o => o.IdCompte == compteId)
            .OrderByDescending(o => o.DateOperation)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();
    }

    public async Task<OperationEpargne> CreateAsync(OperationEpargne operation)
    {
        _context.OperationsEpargne.Add(operation);
        await _context.SaveChangesAsync();
        return operation;
    }
}
