package service.impl;

import config.Constantes;
import dao.EtudiantDAO;
import dao.ModuleDAO;
import dao.NoteDAO;
import exception.ServiceException;
import exception.ValidationException;
import model.Note;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.NoteService;
import util.ExcelImporter;

import java.io.File;
import java.util.List;

public class NoteServiceImpl implements NoteService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoteServiceImpl.class);
    private final NoteDAO noteDAO;
    private final EtudiantDAO etudiantDAO;
    private final ModuleDAO moduleDAO;

    public NoteServiceImpl(NoteDAO noteDAO, EtudiantDAO etudiantDAO, ModuleDAO moduleDAO) {
        this.noteDAO = noteDAO;
        this.etudiantDAO = etudiantDAO;
        this.moduleDAO = moduleDAO;
    }

    @Override
    public Note saisirNote(Note n) {
        validerNote(n);
        if (etudiantDAO.findById(n.getEtudiantId()) == null) throw new ServiceException("Etudiant introuvable");
        if (moduleDAO.findSousModuleById(n.getSousModuleId()) == null) throw new ServiceException("Sous-module introuvable");
        LOGGER.info("Saisie note: etudiant={}, sousModule={}, valeur={}", n.getEtudiantId(), n.getSousModuleId(), n.getValeur());
        return noteDAO.insert(n);
    }

    @Override
    public Note modifierNote(Note n) {
        validerNote(n);
        if (noteDAO.findById(n.getId()) == null) throw new ServiceException("Note introuvable");
        LOGGER.info("Modification note: {}", n.getId());
        return noteDAO.update(n);
    }

    @Override
    public void supprimerNote(Long id) {
        if (noteDAO.findById(id) == null) throw new ServiceException("Note introuvable");
        noteDAO.delete(id);
        LOGGER.info("Suppression note: {}", id);
    }

    @Override
    public int importerNotes(File fichier, Long sousModuleId, Long enseignantId) {
        if (moduleDAO.findSousModuleById(sousModuleId) == null) throw new ServiceException("Sous-module introuvable");
        List<Note> notes = ExcelImporter.importerNotes(fichier, sousModuleId, enseignantId);
        int count = 0;
        for (Note n : notes) {
            try { saisirNote(n); count++; }
            catch (Exception ex) { LOGGER.warn("Note ignoree pour etudiant {}: {}", n.getEtudiantId(), ex.getMessage()); }
        }
        LOGGER.info("Import de {} notes dans sous-module {}", count, sousModuleId);
        return count;
    }

    @Override
    public void validerNotesSousModule(Long sousModuleId) {
        noteDAO.validerParSousModule(sousModuleId);
        LOGGER.info("Validation notes sous-module: {}", sousModuleId);
    }

    @Override
    public List<Note> getNotesParEtudiant(Long etudiantId) { return noteDAO.findByEtudiant(etudiantId); }

    @Override
    public List<Note> getNotesParSousModule(Long sousModuleId) { return noteDAO.findBySousModule(sousModuleId); }

    @Override
    public double getMoyennePonderee(Long etudiantId, Long promotionId) {
        return noteDAO.calculerMoyenneGenerale(etudiantId, promotionId);
    }

    @Override
    public List<Note> getNotesParPromotion(Long promotionId) { return noteDAO.findByPromotion(promotionId); }

    @Override
    public void validerNote(Long noteId) {
        Note n = noteDAO.findById(noteId);
        if (n == null) throw new ServiceException("Note introuvable");
        n.setValidee(true);
        noteDAO.update(n);
        LOGGER.info("Validation note: {}", noteId);
    }

    private void validerNote(Note n) {
        if (n.getValeur() < Constantes.NOTE_MIN || n.getValeur() > Constantes.NOTE_MAX)
            throw new ValidationException("valeur", Constantes.MSG_NOTE_INVALIDE);
        if (n.getEtudiantId() == null) throw new ValidationException("etudiant", "Etudiant obligatoire");
        if (n.getSousModuleId() == null) throw new ValidationException("sousModule", "Sous-module obligatoire");
    }
}
