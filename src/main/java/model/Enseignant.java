package model;

import java.util.Objects;

public class Enseignant extends Utilisateur {
    public enum Grade { ASSISTANT, MAITRE_ASSISTANT, PROFESSEUR }
    private String matricule;
    private String specialite;
    private Grade grade;
    private int nombreSousModules;
    /**
     * FIXED: ID de la table enseignant (PK propre, different de utilisateur.id).
     * Necessaire pour remplir la FK saisi_par dans la table note.
     */
    private Long enseignantPk;

    public Enseignant() { super(); }
    public Enseignant(String login, String motDePasse, String nom, String prenom, String email,
                      String matricule, String specialite, Grade grade) {
        super(login, motDePasse, nom, prenom, email, Role.ENSEIGNANT);
        this.matricule = matricule; this.specialite = specialite; this.grade = grade;
    }

    public String getMatricule() { return matricule; } public void setMatricule(String m) { this.matricule = m; }
    public String getSpecialite() { return specialite; } public void setSpecialite(String s) { this.specialite = s; }
    public Grade getGrade() { return grade; } public void setGrade(Grade grade) { this.grade = grade; }
    public int getNombreSousModules() { return nombreSousModules; } public void setNombreSousModules(int n) { this.nombreSousModules = n; }
    public Long getEnseignantPk() { return enseignantPk; } public void setEnseignantPk(Long pk) { this.enseignantPk = pk; }

    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof Enseignant e)) return false; return super.equals(o) && Objects.equals(matricule, e.matricule); }
    @Override public int hashCode() { return Objects.hash(super.hashCode(), matricule); }
    @Override public String toString() { return getNomComplet() + " (" + matricule + ")"; }
}
