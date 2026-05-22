package util;

import config.Constantes;
import model.Etudiant;
import model.Note;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class ExcelImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelImporter.class);

    private ExcelImporter() {}

    /**
     * Importe une liste d'etudiants depuis un fichier Excel.
     * Colonnes attendues: CNE, Nom, Prenom, DateNaissance, Email, Telephone
     */
    public static List<Etudiant> importerEtudiants(File fichier) {
        List<Etudiant> etudiants = new ArrayList<>();
        try (Workbook wb = ouvrirWorkbook(fichier)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Ignorer l'en-tete
                try {
                    Etudiant e = new Etudiant();
                    e.setCne(getCellString(row, 0));
                    e.setNom(getCellString(row, 1));
                    e.setPrenom(getCellString(row, 2));
                    e.setDateNaissance(getCellDate(row, 3));
                    e.setEmail(getCellString(row, 4));
                    e.setTelephone(getCellString(row, 5));
                    e.setStatut(Etudiant.Statut.ACTIF);
                    if (e.getCne() != null && !e.getCne().isBlank()) {
                        etudiants.add(e);
                    }
                } catch (Exception ex) {
                    LOGGER.warn("Ligne {} ignoree: {}", row.getRowNum() + 1, ex.getMessage());
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Erreur lecture fichier Excel", ex);
            throw new RuntimeException("Erreur lecture fichier: " + ex.getMessage());
        }
        LOGGER.info("{} etudiants importes depuis {}", etudiants.size(), fichier.getName());
        return etudiants;
    }

    /**
     * Importe des notes depuis un fichier Excel.
     * Colonnes attendues: CNE_Etudiant, Valeur, TypeNote
     */
    public static List<Note> importerNotes(File fichier, Long sousModuleId, Long enseignantId) {
        List<Note> notes = new ArrayList<>();
        try (Workbook wb = ouvrirWorkbook(fichier)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                try {
                    String cne = getCellString(row, 0);
                    double valeur = getCellNumeric(row, 1);
                    String typeStr = getCellString(row, 2);
                    if (cne == null || cne.isBlank()) continue;
                    if (!Validator.validerNote(valeur)) continue;

                    Note.TypeNote type = parseTypeNote(typeStr);
                    Note n = new Note(valeur, type, null, sousModuleId, enseignantId);
                    n.setEtudiantCne(cne); // Pour resolution ulterieure
                    notes.add(n);
                } catch (Exception ex) {
                    LOGGER.warn("Ligne {} ignoree: {}", row.getRowNum() + 1, ex.getMessage());
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Erreur lecture fichier Excel", ex);
            throw new RuntimeException("Erreur lecture fichier: " + ex.getMessage());
        }
        LOGGER.info("{} notes importees depuis {}", notes.size(), fichier.getName());
        return notes;
    }

    private static Workbook ouvrirWorkbook(File fichier) throws IOException {
        String name = fichier.getName().toLowerCase();
        FileInputStream fis = new FileInputStream(fichier);
        if (name.endsWith(".xlsx")) {
            return new XSSFWorkbook(fis);
        } else if (name.endsWith(".xls")) {
            return new HSSFWorkbook(fis);
        } else {
            throw new IllegalArgumentException("Format non supporte. Utilisez .xls ou .xlsx");
        }
    }

    private static String getCellString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    private static double getCellNumeric(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try { yield Double.parseDouble(cell.getStringCellValue().replace(",", ".")); }
                catch (NumberFormatException e) { yield 0; }
            }
            default -> 0;
        };
    }

    private static LocalDate getCellDate(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    private static Note.TypeNote parseTypeNote(String str) {
        if (str == null) return Note.TypeNote.EXAMEN;
        return switch (str.trim().toUpperCase()) {
            case "TP", "TRAVAIL PRATIQUE" -> Note.TypeNote.TP;
            case "CC", "CONTROLE CONTINU", "CONTROLE" -> Note.TypeNote.CONTROLE_CONTINU;
            case "PROJET" -> Note.TypeNote.PROJET;
            default -> Note.TypeNote.EXAMEN;
        };
    }
}
