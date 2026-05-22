package util;

import config.Constantes;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Validations metier.
 * FIXED: regex CNE plus permissive, meilleurs messages d'erreur.
 */
public final class Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(Constantes.REGEX_EMAIL);
    private static final Pattern TELEPHONE_PATTERN = Pattern.compile(Constantes.REGEX_TELEPHONE);
    private static final Pattern CNE_PATTERN = Pattern.compile(Constantes.REGEX_CNE);

    private Validator() {}

    public static boolean validerEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean validerTelephone(String telephone) {
        return telephone != null && TELEPHONE_PATTERN.matcher(telephone).matches();
    }

    public static boolean validerCne(String cne) {
        return cne != null && CNE_PATTERN.matcher(cne).matches();
    }

    public static boolean validerNote(double note) {
        return note >= Constantes.NOTE_MIN && note <= Constantes.NOTE_MAX;
    }

    public static boolean validerDateNaissance(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    public static boolean validerChaineNonVide(String chaine) {
        return chaine != null && !chaine.isBlank();
    }

    public static boolean validerEntierPositif(int valeur) {
        return valeur > 0;
    }

    /** FIXED: validation complete d'un etudiant avec messages explicites */
    public static String validerEtudiant(model.Etudiant e) {
        if (!validerChaineNonVide(e.getCne())) return "CNE obligatoire";
        if (!validerCne(e.getCne())) return Constantes.MSG_CNE_INVALIDE;
        if (!validerChaineNonVide(e.getNom())) return "Nom obligatoire";
        if (!validerChaineNonVide(e.getPrenom())) return "Prenom obligatoire";
        if (e.getEmail() != null && !e.getEmail().isBlank() && !validerEmail(e.getEmail()))
            return Constantes.MSG_EMAIL_INVALIDE;
        if (e.getTelephone() != null && !e.getTelephone().isBlank() && !validerTelephone(e.getTelephone()))
            return Constantes.MSG_TELEPHONE_INVALIDE;
        if (e.getDateNaissance() != null && !validerDateNaissance(e.getDateNaissance()))
            return Constantes.MSG_DATE_INVALIDE;
        return null; // tout est valide
    }
}
