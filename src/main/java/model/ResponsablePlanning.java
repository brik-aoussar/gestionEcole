package model;

public class ResponsablePlanning extends Utilisateur {
    private String matricule;
    private String departement;

    public ResponsablePlanning() { super(); }
    public ResponsablePlanning(String login, String motDePasse, String nom, String prenom, String email,
                                String matricule, String departement) {
        super(login, motDePasse, nom, prenom, email, Role.RESPONSABLE_PLANNING);
        this.matricule = matricule; this.departement = departement;
    }

    public String getMatricule() { return matricule; } public void setMatricule(String m) { this.matricule = m; }
    public String getDepartement() { return departement; } public void setDepartement(String d) { this.departement = d; }
}
