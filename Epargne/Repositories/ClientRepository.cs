using Microsoft.EntityFrameworkCore;
using Epargne.Data;
using Epargne.Models.Entities;

namespace Epargne.Repositories;

/// <summary>
/// Repository pour les opérations sur les clients
/// </summary>
public interface IClientRepository
{
    Task<Client?> GetByIdAsync(long id);
    Task<Client?> GetByEmailAsync(string email);
    Task<Client?> GetByNumeroClientAsync(string numeroClient);
    Task<Client?> GetByNumCinAsync(string numCin);
    Task<IEnumerable<Client>> GetAllAsync();
    Task<Client> CreateAsync(Client client);
    Task<Client> UpdateAsync(Client client);
    Task DeleteAsync(long id);
    Task<bool> ExistsByEmailAsync(string email);
    Task<bool> ExistsByNumCinAsync(string numCin);
}

public class ClientRepository : IClientRepository
{
    private readonly EpargneDbContext _context;

    public ClientRepository(EpargneDbContext context)
    {
        _context = context;
    }

    public async Task<Client?> GetByIdAsync(long id)
    {
        return await _context.Clients
            .Include(c => c.ComptesEpargne)
            .ThenInclude(ce => ce.TypeCompte)
            .FirstOrDefaultAsync(c => c.IdClient == id);
    }

    public async Task<Client?> GetByEmailAsync(string email)
    {
        return await _context.Clients
            .FirstOrDefaultAsync(c => c.Email == email);
    }

    public async Task<Client?> GetByNumeroClientAsync(string numeroClient)
    {
        return await _context.Clients
            .Include(c => c.ComptesEpargne)
            .FirstOrDefaultAsync(c => c.NumeroClient == numeroClient);
    }

    public async Task<Client?> GetByNumCinAsync(string numCin)
    {
        return await _context.Clients
            .FirstOrDefaultAsync(c => c.NumCin == numCin);
    }

    public async Task<IEnumerable<Client>> GetAllAsync()
    {
        return await _context.Clients
            .Include(c => c.ComptesEpargne)
            .ToListAsync();
    }

    public async Task<Client> CreateAsync(Client client)
    {
        _context.Clients.Add(client);
        await _context.SaveChangesAsync();
        return client;
    }

    public async Task<Client> UpdateAsync(Client client)
    {
        client.DateModification = DateTime.UtcNow;
        _context.Clients.Update(client);
        await _context.SaveChangesAsync();
        return client;
    }

    public async Task DeleteAsync(long id)
    {
        var client = await _context.Clients.FindAsync(id);
        if (client != null)
        {
            _context.Clients.Remove(client);
            await _context.SaveChangesAsync();
        }
    }

    public async Task<bool> ExistsByEmailAsync(string email)
    {
        return await _context.Clients.AnyAsync(c => c.Email == email);
    }

    public async Task<bool> ExistsByNumCinAsync(string numCin)
    {
        return await _context.Clients.AnyAsync(c => c.NumCin == numCin);
    }
}
