package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Note {
    public enum TypeNote { EXAMEN, TP, CONTROLE_CONTINU, PROJET }
    private Long id;
    private double valeur;
    private TypeNote typeNote;
    private Long etudiantId;
    private Long sousModuleId;
    private Long saisiPar;
    private LocalDateTime dateSaisie;
    private boolean validee;
    private String etudiantNom;
    private String etudiantCne;
    private String sousModuleIntitule;
    private String moduleIntitule;

    public Note() {}
    public Note(double valeur, TypeNote typeNote, Long etudiantId, Long sousModuleId, Long saisiPar) {
        this.valeur = valeur; this.typeNote = typeNote; this.etudiantId = etudiantId;
        this.sousModuleId = sousModuleId; this.saisiPar = saisiPar; this.validee = false;
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public double getValeur() { return valeur; } public void setValeur(double valeur) { this.valeur = valeur; }
    public TypeNote getTypeNote() { return typeNote; } public void setTypeNote(TypeNote typeNote) { this.typeNote = typeNote; }
    public Long getEtudiantId() { return etudiantId; } public void setEtudiantId(Long etudiantId) { this.etudiantId = etudiantId; }
    public Long getSousModuleId() { return sousModuleId; } public void setSousModuleId(Long sousModuleId) { this.sousModuleId = sousModuleId; }
    public Long getSaisiPar() { return saisiPar; } public void setSaisiPar(Long saisiPar) { this.saisiPar = saisiPar; }
    public LocalDateTime getDateSaisie() { return dateSaisie; } public void setDateSaisie(LocalDateTime d) { this.dateSaisie = d; }
    public boolean isValidee() { return validee; } public void setValidee(boolean validee) { this.validee = validee; }
    public String getEtudiantNom() { return etudiantNom; } public void setEtudiantNom(String n) { this.etudiantNom = n; }
    public String getEtudiantCne() { return etudiantCne; } public void setEtudiantCne(String c) { this.etudiantCne = c; }
    public String getSousModuleIntitule() { return sousModuleIntitule; } public void setSousModuleIntitule(String s) { this.sousModuleIntitule = s; }
    public String getModuleIntitule() { return moduleIntitule; } public void setModuleIntitule(String m) { this.moduleIntitule = m; }

    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof Note n)) return false; return Objects.equals(id, n.id); }
    @Override public int hashCode() { return Objects.hash(id); }
    @Override public String toString() { return "Note{id=" + id + ", valeur=" + valeur + ", type=" + typeNote + "}"; }
}
