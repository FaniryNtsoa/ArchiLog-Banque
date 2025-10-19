package com.banque.centralisateur.util;

import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import java.math.BigDecimal;

/**
 * Utilitaire pour faciliter l'extraction de valeurs depuis des objets JSON
 * Gère automatiquement la conversion entre PascalCase (.NET) et camelCase (Java)
 */
public class JsonHelper {
    
    /**
     * Récupère une valeur String depuis un JsonObject en essayant différentes conventions de nommage
     * @param json L'objet JSON
     * @param key La clé (en camelCase)
     * @return La valeur ou null si non trouvée
     */
    public static String getSafeString(JsonObject json, String key) {
        return getSafeString(json, key, null);
    }
    
    /**
     * Récupère une valeur String depuis un JsonObject avec une valeur par défaut
     * @param json L'objet JSON
     * @param key La clé (en camelCase)
     * @param defaultValue Valeur par défaut si non trouvée
     * @return La valeur ou defaultValue si non trouvée
     */
    public static String getSafeString(JsonObject json, String key, String defaultValue) {
        // Essayer camelCase d'abord
        if (json.containsKey(key)) {
            JsonValue value = json.get(key);
            if (value != null && value.getValueType() == JsonValue.ValueType.STRING) {
                return ((JsonString) value).getString();
            }
        }
        
        // Essayer PascalCase (première lettre en majuscule)
        String pascalKey = toPascalCase(key);
        if (json.containsKey(pascalKey)) {
            JsonValue value = json.get(pascalKey);
            if (value != null && value.getValueType() == JsonValue.ValueType.STRING) {
                return ((JsonString) value).getString();
            }
        }
        
        return defaultValue;
    }
    
    /**
     * Récupère une valeur Long depuis un JsonObject
     * @param json L'objet JSON
     * @param key La clé (en camelCase)
     * @return La valeur ou null si non trouvée
     */
    public static Long getSafeLong(JsonObject json, String key) {
        return getSafeLong(json, key, null);
    }
    
    /**
     * Récupère une valeur Long depuis un JsonObject avec une valeur par défaut
     * @param json L'objet JSON
     * @param key La clé (en camelCase)
     * @param defaultValue Valeur par défaut si non trouvée
     * @return La valeur ou defaultValue si non trouvée
     */
    public static Long getSafeLong(JsonObject json, String key, Long defaultValue) {
        // Essayer camelCase
        JsonNumber number = json.getJsonNumber(key);
        if (number != null) {
            return number.longValue();
        }
        
        // Essayer PascalCase
        String pascalKey = toPascalCase(key);
        number = json.getJsonNumber(pascalKey);
        if (number != null) {
            return number.longValue();
        }
        
        return defaultValue;
    }
    
    /**
     * Récupère une valeur Integer depuis un JsonObject
     * @param json L'objet JSON
     * @param key La clé (en camelCase)
     * @return La valeur ou null si non trouvée
     */
    public static Integer getSafeInt(JsonObject json, String key) {
        return getSafeInt(json, key, null);
    }
    
    /**
     * Récupère une valeur Integer depuis un JsonObject avec une valeur par défaut
     * @param json L'objet JSON
     * @param key La clé (en camelCase)
     * @param defaultValue Valeur par défaut si non trouvée
     * @return La valeur ou defaultValue si non trouvée
     */
    public static Integer getSafeInt(JsonObject json, String key, Integer defaultValue) {
        // Essayer camelCase
        JsonNumber number = json.getJsonNumber(key);
        if (number != null) {
            return number.intValue();
        }
        
        // Essayer PascalCase
        String pascalKey = toPascalCase(key);
        number = json.getJsonNumber(pascalKey);
        if (number != null) {
            return number.intValue();
        }
        
        return defaultValue;
    }
    
    /**
     * Récupère une valeur BigDecimal depuis un JsonObject
     * @param json L'objet JSON
     * @param key La clé (en camelCase)
     * @return La valeur ou null si non trouvée
     */
    public static BigDecimal getSafeBigDecimal(JsonObject json, String key) {
        return getSafeBigDecimal(json, key, null);
    }
    
    /**
     * Récupère une valeur BigDecimal depuis un JsonObject avec une valeur par défaut
     * @param json L'objet JSON
     * @param key La clé (en camelCase)
     * @param defaultValue Valeur par défaut si non trouvée
     * @return La valeur ou defaultValue si non trouvée
     */
    public static BigDecimal getSafeBigDecimal(JsonObject json, String key, BigDecimal defaultValue) {
        // Essayer camelCase
        JsonNumber number = json.getJsonNumber(key);
        if (number != null) {
            return number.bigDecimalValue();
        }
        
        // Essayer PascalCase
        String pascalKey = toPascalCase(key);
        number = json.getJsonNumber(pascalKey);
        if (number != null) {
            return number.bigDecimalValue();
        }
        
        return defaultValue;
    }
    
    /**
     * Récupère une valeur Boolean depuis un JsonObject
     * @param json L'objet JSON
     * @param key La clé (en camelCase)
     * @return La valeur ou false si non trouvée
     */
    public static Boolean getSafeBoolean(JsonObject json, String key) {
        return getSafeBoolean(json, key, false);
    }
    
    /**
     * Récupère une valeur Boolean depuis un JsonObject avec une valeur par défaut
     * @param json L'objet JSON
     * @param key La clé (en camelCase)
     * @param defaultValue Valeur par défaut si non trouvée
     * @return La valeur ou defaultValue si non trouvée
     */
    public static Boolean getSafeBoolean(JsonObject json, String key, Boolean defaultValue) {
        // Essayer camelCase
        if (json.containsKey(key)) {
            return json.getBoolean(key, defaultValue);
        }
        
        // Essayer PascalCase
        String pascalKey = toPascalCase(key);
        if (json.containsKey(pascalKey)) {
            return json.getBoolean(pascalKey, defaultValue);
        }
        
        return defaultValue;
    }
    
    /**
     * Convertit une chaîne en PascalCase
     * Exemples:
     *   idTypeCompte -> IdTypeCompte
     *   numeroCompte -> NumeroCompte
     *   libelle -> Libelle
     */
    private static String toPascalCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        return Character.toUpperCase(camelCase.charAt(0)) + camelCase.substring(1);
    }
}
