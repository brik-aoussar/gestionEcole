package util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import config.AppConfig;
import config.Constantes;
import model.Etudiant;
import model.Note;

import java.awt.Color;  // FIXED: was com.lowagie.text.Color which doesn't exist in OpenPDF
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public final class PDFExporter {

    private static final String FONT = "Helvetica";
    private static final int TAILLE_TITRE = 18;
    private static final int TAILLE_SOUS_TITRE = 14;
    private static final int TAILLE_NORMALE = 10;

    private PDFExporter() {}

    public static File exporterListeEtudiants(List<Etudiant> etudiants, String promotion) throws IOException {
        String chemin = AppConfig.get().getExportPath() + "liste_etudiants_" + promotion.replace(" ", "_") + "_" + LocalDate.now() + ".pdf";
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(chemin));
        doc.open();

        Paragraph titre = new Paragraph("Liste des Etudiants", new Font(Font.HELVETICA, TAILLE_TITRE, Font.BOLD));
        titre.setAlignment(Element.ALIGN_CENTER);
        doc.add(titre);
        doc.add(new Paragraph("Promotion: " + promotion, new Font(Font.HELVETICA, TAILLE_SOUS_TITRE)));
        doc.add(new Paragraph("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern(Constantes.FORMAT_DATE))));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        String[] headers = {"CNE", "Nom", "Prenom", "Email", "Statut"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, TAILLE_NORMALE, Font.BOLD)));
            cell.setBackgroundColor(new Color(200, 200, 200));  // FIXED: java.awt.Color
            table.addCell(cell);
        }
        for (Etudiant e : etudiants) {
            table.addCell(e.getCne());
            table.addCell(e.getNom());
            table.addCell(e.getPrenom());
            table.addCell(e.getEmail() != null ? e.getEmail() : "");
            table.addCell(e.getStatut().toString());
        }
        doc.add(table);
        doc.close();
        return new File(chemin);
    }

    public static File exporterReleveNotes(Etudiant etudiant, List<Note> notes,
                                           double moyenneGenerale, String promotion) throws IOException {
        String chemin = AppConfig.get().getExportPath() + "releve_" + etudiant.getCne() + "_" + LocalDate.now() + ".pdf";
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(chemin));
        doc.open();

        Paragraph titre = new Paragraph("Releve de Notes", new Font(Font.HELVETICA, TAILLE_TITRE, Font.BOLD));
        titre.setAlignment(Element.ALIGN_CENTER);
        doc.add(titre);
        doc.add(new Paragraph("Etudiant: " + etudiant.getNomComplet()));
        doc.add(new Paragraph("CNE: " + etudiant.getCne()));
        doc.add(new Paragraph("Promotion: " + promotion));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        String[] headers = {"Module", "Sous-Module", "Note", "Type"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, TAILLE_NORMALE, Font.BOLD)));
            cell.setBackgroundColor(new Color(200, 200, 200));  // FIXED: java.awt.Color
            table.addCell(cell);
        }

        String currentModule = "";
        for (Note n : notes) {
            if (n.getModuleIntitule() != null && !n.getModuleIntitule().equals(currentModule)) {
                currentModule = n.getModuleIntitule();
                PdfPCell cell = new PdfPCell(new Phrase(currentModule, new Font(Font.HELVETICA, TAILLE_NORMALE, Font.BOLD)));
                cell.setColspan(4);
                cell.setBackgroundColor(new Color(230, 230, 230));  // FIXED: java.awt.Color
                table.addCell(cell);
            }
            table.addCell("");
            table.addCell(n.getSousModuleIntitule() != null ? n.getSousModuleIntitule() : "");
            table.addCell(String.format("%.2f", n.getValeur()));
            table.addCell(n.getTypeNote() != null ? n.getTypeNote().toString() : "");
        }
        doc.add(table);

        doc.add(Chunk.NEWLINE);
        Paragraph moy = new Paragraph("Moyenne Generale: " + String.format("%.2f", moyenneGenerale) + "/20",
                new Font(Font.HELVETICA, TAILLE_SOUS_TITRE, Font.BOLD));
        moy.setAlignment(Element.ALIGN_RIGHT);
        doc.add(moy);

        String mention = calculerMention(moyenneGenerale);
        Paragraph men = new Paragraph("Mention: " + mention, new Font(Font.HELVETICA, TAILLE_NORMALE, Font.ITALIC));
        men.setAlignment(Element.ALIGN_RIGHT);
        doc.add(men);

        doc.close();
        return new File(chemin);
    }

    public static File exporterRapportClassement(List<Map<String, Object>> classement,
                                                  String promotion) throws IOException {
        String chemin = AppConfig.get().getExportPath() + "classement_" + promotion.replace(" ", "_") + "_" + LocalDate.now() + ".pdf";
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(chemin));
        doc.open();

        Paragraph titre = new Paragraph("Classement de la Promotion", new Font(Font.HELVETICA, TAILLE_TITRE, Font.BOLD));
        titre.setAlignment(Element.ALIGN_CENTER);
        doc.add(titre);
        doc.add(new Paragraph("Promotion: " + promotion));
        doc.add(new Paragraph("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern(Constantes.FORMAT_DATE))));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        String[] headers = {"Rang", "CNE", "Nom Complet", "Moyenne", "Mention"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, TAILLE_NORMALE, Font.BOLD)));
            cell.setBackgroundColor(new Color(200, 200, 200));  // FIXED: java.awt.Color
            table.addCell(cell);
        }

        int rang = 1;
        for (Map<String, Object> row : classement) {
            table.addCell(String.valueOf(rang++));
            table.addCell(String.valueOf(row.get("cne")));
            table.addCell(String.valueOf(row.get("nom_complet")));
            double moy = ((Number) row.get("moyenne_generale")).doubleValue();
            table.addCell(String.format("%.2f", moy));
            table.addCell(calculerMention(moy));
        }
        doc.add(table);
        doc.close();
        return new File(chemin);
    }

    private static String calculerMention(double moyenne) {
        if (moyenne >= Constantes.MENTION_TRES_BIEN) return "Tres Bien";
        if (moyenne >= Constantes.MENTION_BIEN) return "Bien";
        if (moyenne >= Constantes.MENTION_ASSEZ_BIEN) return "Assez Bien";
        if (moyenne >= Constantes.MENTION_PASSABLE) return "Passable";
        return "Non Valide";
    }
}
