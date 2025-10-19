package com.banque.pret.util;

import com.banque.pret.dto.EcheanceDTO;
import com.banque.pret.entity.enums.StatutEcheance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire pour les calculs de prêt
 */
public class CalculPretUtil {

    /**
     * Calcule la mensualité d'un prêt selon la formule classique
     * M = C * (t / (1 - (1 + t)^-n))
     * où M = mensualité, C = capital, t = taux mensuel, n = nombre de mois
     */
    public static BigDecimal calculerMensualite(BigDecimal montant, BigDecimal tauxAnnuel, Integer dureeMois) {
        if (montant == null || tauxAnnuel == null || dureeMois == null || dureeMois <= 0) {
            throw new IllegalArgumentException("Paramètres invalides pour le calcul de la mensualité");
        }

        // Taux mensuel
        BigDecimal tauxMensuel = tauxAnnuel.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                                           .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        // Si taux = 0, mensualité simple
        if (tauxMensuel.compareTo(BigDecimal.ZERO) == 0) {
            return montant.divide(BigDecimal.valueOf(dureeMois), 2, RoundingMode.HALF_UP);
        }

        // Formule : M = C * (t / (1 - (1 + t)^-n))
        BigDecimal unPlusTaux = BigDecimal.ONE.add(tauxMensuel);
        BigDecimal puissance = BigDecimal.valueOf(Math.pow(unPlusTaux.doubleValue(), -dureeMois));
        BigDecimal denominateur = BigDecimal.ONE.subtract(puissance);
        BigDecimal mensualite = montant.multiply(tauxMensuel).divide(denominateur, 2, RoundingMode.HALF_UP);

        return mensualite;
    }

    /**
     * Génère le tableau d'amortissement complet
     */
    public static List<EcheanceDTO> genererTableauAmortissement(
            BigDecimal montant, 
            BigDecimal tauxAnnuel, 
            Integer dureeMois,
            LocalDate datePremiereEcheance) {
        
        if (montant == null || tauxAnnuel == null || dureeMois == null || datePremiereEcheance == null) {
            throw new IllegalArgumentException("Paramètres invalides pour la génération du tableau d'amortissement");
        }

        List<EcheanceDTO> tableau = new ArrayList<>();
        
        BigDecimal mensualite = calculerMensualite(montant, tauxAnnuel, dureeMois);
        BigDecimal tauxMensuel = tauxAnnuel.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                                           .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        
        BigDecimal capitalRestant = montant;
        LocalDate dateEcheance = datePremiereEcheance;

        for (int i = 1; i <= dureeMois; i++) {
            // Calcul des intérêts
            BigDecimal interet = capitalRestant.multiply(tauxMensuel).setScale(2, RoundingMode.HALF_UP);
            
            // Calcul du capital remboursé
            BigDecimal capital = mensualite.subtract(interet);
            
            // Ajustement pour la dernière échéance (arrondi)
            if (i == dureeMois) {
                capital = capitalRestant;
                mensualite = capital.add(interet);
            }
            
            // Nouveau capital restant
            capitalRestant = capitalRestant.subtract(capital);
            
            // Création de l'échéance
            EcheanceDTO echeance = EcheanceDTO.builder()
                    .numeroEcheance(i)
                    .montantEcheance(mensualite)
                    .capital(capital)
                    .interet(interet)
                    .capitalRestant(capitalRestant)
                    .dateEcheance(dateEcheance)
                    .statut(StatutEcheance.A_VENIR.name())
                    .penaliteAppliquee(BigDecimal.ZERO)
                    .joursRetard(0)
                    .build();
            
            tableau.add(echeance);
            
            // Date de la prochaine échéance
            dateEcheance = dateEcheance.plusMonths(1);
        }

        return tableau;
    }

    /**
     * Calcule le coût total du crédit
     */
    public static BigDecimal calculerCoutTotalCredit(BigDecimal montantTotalDu, BigDecimal montantEmprunte, BigDecimal fraisDossier) {
        if (montantTotalDu == null || montantEmprunte == null) {
            throw new IllegalArgumentException("Paramètres invalides pour le calcul du coût total");
        }
        
        BigDecimal frais = fraisDossier != null ? fraisDossier : BigDecimal.ZERO;
        return montantTotalDu.subtract(montantEmprunte).add(frais);
    }

    /**
     * Calcule le montant total à rembourser
     */
    public static BigDecimal calculerMontantTotalDu(BigDecimal mensualite, Integer dureeMois) {
        if (mensualite == null || dureeMois == null) {
            throw new IllegalArgumentException("Paramètres invalides pour le calcul du montant total");
        }
        
        return mensualite.multiply(BigDecimal.valueOf(dureeMois)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcule le total des intérêts
     */
    public static BigDecimal calculerTotalInterets(List<EcheanceDTO> tableau) {
        if (tableau == null || tableau.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return tableau.stream()
                .map(EcheanceDTO::getInteret)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Vérifie si un montant est dans les limites autorisées
     */
    public static boolean estMontantValide(BigDecimal montant, BigDecimal montantMin, BigDecimal montantMax) {
        if (montant == null || montantMin == null || montantMax == null) {
            return false;
        }
        
        return montant.compareTo(montantMin) >= 0 && montant.compareTo(montantMax) <= 0;
    }

    /**
     * Vérifie si une durée est dans les limites autorisées
     */
    public static boolean estDureeValide(Integer duree, Integer dureeMin, Integer dureeMax) {
        if (duree == null || dureeMin == null || dureeMax == null) {
            return false;
        }
        
        return duree >= dureeMin && duree <= dureeMax;
    }
}
