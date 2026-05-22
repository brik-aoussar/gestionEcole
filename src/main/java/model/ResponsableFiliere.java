package model;

public class ResponsableFiliere extends Utilisateur {
    private String matricule;
    private Long filiereId;
    private String filiereIntitule;

    public ResponsableFiliere() { super(); }
    public ResponsableFiliere(String login, String motDePasse, String nom, String prenom, String email,
                               String matricule, Long filiereId) {
        super(login, motDePasse, nom, prenom, email, Role.RESPONSABLE_FILIERE);
        this.matricule = matricule; this.filiereId = filiereId;
    }

    public String getMatricule() { return matricule; } public void setMatricule(String m) { this.matricule = m; }
    public Long getFiliereId() { return filiereId; } public void setFiliereId(Long fid) { this.filiereId = fid; }
    public String getFiliereIntitule() { return filiereIntitule; } public void setFiliereIntitule(String f) { this.filiereIntitule = f; }
}
