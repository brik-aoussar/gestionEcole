package model;

import java.util.Objects;

public class Filiere {
    private Long id;
    private String code;
    private String intitule;
    private String domaine;
    private int nombrePromotions;

    public Filiere() {}
    public Filiere(String code, String intitule, String domaine) {
        this.code = code; this.intitule = intitule; this.domaine = domaine;
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getCode() { return code; } public void setCode(String code) { this.code = code; }
    public String getIntitule() { return intitule; } public void setIntitule(String intitule) { this.intitule = intitule; }
    public String getDomaine() { return domaine; } public void setDomaine(String domaine) { this.domaine = domaine; }
    public int getNombrePromotions() { return nombrePromotions; } public void setNombrePromotions(int n) { this.nombrePromotions = n; }

    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof Filiere f)) return false; return Objects.equals(id, f.id); }
    @Override public int hashCode() { return Objects.hash(id); }
    @Override public String toString() { return code + " - " + intitule; }
}
