package util;

import config.Constantes;

import java.time.LocalDate;
import java.util.regex.Pattern;

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
}
