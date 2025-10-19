using Microsoft.EntityFrameworkCore;
using Epargne.Data;
using Epargne.Repositories;
using Epargne.Services;

// Configuration globale pour PostgreSQL - tous les DateTime en UTC
AppContext.SetSwitch("Npgsql.EnableLegacyTimestampBehavior", true);

var builder = WebApplication.CreateBuilder(args);

// Configuration de la base de données PostgreSQL
builder.Services.AddDbContext<EpargneDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));

// Enregistrement des repositories
builder.Services.AddScoped<IClientRepository, ClientRepository>();
builder.Services.AddScoped<ITypeCompteEpargneRepository, TypeCompteEpargneRepository>();
builder.Services.AddScoped<ICompteEpargneRepository, CompteEpargneRepository>();
builder.Services.AddScoped<IOperationEpargneRepository, OperationEpargneRepository>();
builder.Services.AddScoped<IInteretEpargneRepository, InteretEpargneRepository>();

// Enregistrement des services
builder.Services.AddScoped<IClientService, ClientService>();
builder.Services.AddScoped<ITypeCompteEpargneService, TypeCompteEpargneService>();
builder.Services.AddScoped<ICompteEpargneService, CompteEpargneService>();
builder.Services.AddScoped<IInteretService, InteretService>();

// Add services to the container
builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.PropertyNamingPolicy = null; // Garde les noms de propriétés tels quels
    });

// Configuration de CORS pour permettre au centralisateur de communiquer
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

// Configuration Swagger/OpenAPI
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new Microsoft.OpenApi.Models.OpenApiInfo
    {
        Title = "API Épargne",
        Version = "v1",
        Description = "API REST pour la gestion des comptes épargne - Module Banque",
        Contact = new Microsoft.OpenApi.Models.OpenApiContact
        {
            Name = "Module Épargne",
            Email = "contact@banque.com"
        }
    });
});

var app = builder.Build();

// Créer la base de données si elle n'existe pas
using (var scope = app.Services.CreateScope())
{
    var services = scope.ServiceProvider;
    try
    {
        var context = services.GetRequiredService<EpargneDbContext>();
        context.Database.Migrate();
        app.Logger.LogInformation("Base de données créée/migrée avec succès");
    }
    catch (Exception ex)
    {
        app.Logger.LogError(ex, "Une erreur est survenue lors de la création de la base de données");
    }
}

// Configure the HTTP request pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "API Épargne v1");
        c.RoutePrefix = string.Empty; // Swagger à la racine
    });
}

app.UseHttpsRedirection();
app.UseCors("AllowAll");
app.UseAuthorization();
app.MapControllers();

app.Logger.LogInformation("API Épargne démarrée sur le port configuré");

app.Run();
