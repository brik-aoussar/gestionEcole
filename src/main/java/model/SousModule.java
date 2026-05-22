package model;

import java.util.Objects;

public class SousModule {
    private Long id;
    private String code;
    private String intitule;
    private double coefficient;
    private Long moduleId;
    private Long enseignantId;
    private String enseignantNom;
    private String moduleIntitule;

    public SousModule() {}
    public SousModule(String code, String intitule, double coefficient, Long moduleId, Long enseignantId) {
        this.code = code; this.intitule = intitule; this.coefficient = coefficient;
        this.moduleId = moduleId; this.enseignantId = enseignantId;
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getCode() { return code; } public void setCode(String code) { this.code = code; }
    public String getIntitule() { return intitule; } public void setIntitule(String intitule) { this.intitule = intitule; }
    public double getCoefficient() { return coefficient; } public void setCoefficient(double c) { this.coefficient = c; }
    public Long getModuleId() { return moduleId; } public void setModuleId(Long moduleId) { this.moduleId = moduleId; }
    public Long getEnseignantId() { return enseignantId; } public void setEnseignantId(Long eid) { this.enseignantId = eid; }
    public String getEnseignantNom() { return enseignantNom; } public void setEnseignantNom(String n) { this.enseignantNom = n; }
    public String getModuleIntitule() { return moduleIntitule; } public void setModuleIntitule(String m) { this.moduleIntitule = m; }

    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof SousModule s)) return false; return Objects.equals(id, s.id); }
    @Override public int hashCode() { return Objects.hash(id); }
    @Override public String toString() { return code + " - " + intitule; }
}
