package config;

/**
 * Constantes globales de l'application.
 * Toute valeur "magique" doit etre declaree ici.
 */
public final class Constantes {

    private Constantes() {}

    // -- Regles metier --------------------------------------------------------
    public static final double MOYENNE_REUSSITE = 10.0;
    public static final double COEFF_ELIMINATOIRE = 4.0;
    public static final double NOTE_MIN = 0.0;
    public static final double NOTE_MAX = 20.0;

    // -- Pagination -----------------------------------------------------------
    public static final int MAX_ETUDIANTS_PAR_PAGE = 50;
    public static final int MAX_RESULTATS_RECHERCHE = 100;

    // -- Roles ----------------------------------------------------------------
    public static final String ROLE_RESPONSABLE_PLANNING = "RESPONSABLE_PLANNING";
    public static final String ROLE_RESPONSABLE_FILIERE = "RESPONSABLE_FILIERE";
    public static final String ROLE_ENSEIGNANT = "ENSEIGNANT";

    // -- Statuts etudiant -----------------------------------------------------
    public static final String STATUT_ACTIF = "ACTIF";
    public static final String STATUT_ARCHIVE = "ARCHIVE";
    public static final String STATUT_SUSPENDU = "SUSPENDU";

    // -- Types de note --------------------------------------------------------
    public static final String TYPE_EXAMEN = "EXAMEN";
    public static final String TYPE_TP = "TP";
    public static final String TYPE_CONTROLE_CONTINU = "CONTROLE_CONTINU";
    public static final String TYPE_PROJET = "PROJET";

    // -- Mentions -------------------------------------------------------------
    public static final double MENTION_TRES_BIEN = 16.0;
    public static final double MENTION_BIEN = 14.0;
    public static final double MENTION_ASSEZ_BIEN = 12.0;
    public static final double MENTION_PASSABLE = 10.0;

    // -- Formats --------------------------------------------------------------
    public static final String FORMAT_DATE = "dd/MM/yyyy";
    public static final String FORMAT_DATETIME = "dd/MM/yyyy HH:mm";
    public static final String FORMAT_ANNEE = "yyyy";

    // Email regex (RFC 5322 simplifie)
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    // CNE: lettres suivies de chiffres (ex: AB123456, G123456789)
    public static final String REGEX_CNE = "^[A-Za-z]{1,3}[0-9]{5,10}$";

    // Telephone: format marocain et international
    public static final String REGEX_TELEPHONE = "^(\\+212[\\s]?[5-7][\\s]?[0-9]{8}|0[5-7][\\s]?[0-9]{8})$";

    // -- Fichiers export ------------------------------------------------------
    public static final String EXT_PDF = ".pdf";
    public static final String EXT_EXCEL = ".xlsx";
    public static final String EXT_EXCEL_OLD = ".xls";

    // -- Messages utilisateur -------------------------------------------------
    public static final String MSG_CHAMP_OBLIGATOIRE = "Ce champ est obligatoire";
    public static final String MSG_EMAIL_INVALIDE = "Format d'email invalide";
    public static final String MSG_TELEPHONE_INVALIDE = "Format de telephone invalide (ex: 0612345678 ou +212612345678)";
    public static final String MSG_NOTE_INVALIDE = "La note doit etre comprise entre 0 et 20";
    public static final String MSG_DATE_INVALIDE = "La date doit etre dans le passe";
    public static final String MSG_CNE_INVALIDE = "Format CNE invalide (ex: AB123456)";
    public static final String MSG_SUCCES_ENREGISTREMENT = "Enregistrement effectue avec succes";
    public static final String MSG_SUCCES_SUPPRESSION = "Suppression effectuee avec succes";
    public static final String MSG_CONFIRMATION_ARCHIVAGE = "Confirmer l'archivage de cet etudiant ?";
    public static final String MSG_CONFIRMATION_SUPPRESSION = "Cette action est irreversible. Confirmer ?";
    public static final String MSG_ERREUR_CONNEXION = "Erreur de connexion - verifiez vos identifiants";
    public static final String MSG_COMPTE_DESACTIVE = "Compte desactive - contactez l'administrateur";

    // -- Styles CSS -----------------------------------------------------------
    public static final String CSS_SUCCESS = "-fx-text-fill: #27ae60;";
    public static final String CSS_ERROR = "-fx-text-fill: #e74c3c;";
    public static final String CSS_WARNING = "-fx-text-fill: #f39c12;";

    // -- Application ----------------------------------------------------------
    public static final String APP_NAME = "Gestion des Notes";
    public static final String APP_VERSION = "2.0.0";
    public static final int APP_MIN_WIDTH = 1024;
    public static final int APP_MIN_HEIGHT = 768;
}
