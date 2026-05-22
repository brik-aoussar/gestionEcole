# Gestion des Notes - Application JavaFX

## Version 2.0.0 - Corrections et Ameliorations Completes

### Architecture
- **Couches**: Model -> DAO -> Service -> Controller -> View (FXML)
- **DI**: ServiceLocator (manuel) pour l'injection de dependances
- **Securite**: BCrypt pour le hachage des mots de passe
- **Base de donnees**: MySQL 8 avec HikariCP (pool de connexions)

---

## Corrections de Bugs (vs version originale)

### 1. Securite - CRITIQUE
- **Probleme**: SHA-256 sans salt pour les mots de passe
- **Solution**: BCrypt avec salt automatique (jbcrypt)
- **Impact**: Les mots de passe sont maintenant securises

### 2. Authentification - CRITIQUE
- **Probleme**: Comparaison de hash avec equalsIgnoreCase (bug fatal)
- **Solution**: BCrypt.checkpw() pour verification securisee
- **Impact**: La connexion fonctionne correctement

### 3. Base de donnees
- **Probleme**: Credentials en dur dans le code
- **Solution**: Chargement depuis config.properties
- **Impact**: Configuration flexible

### 4. Statistiques
- **Probleme**: getRapportParFiliere() passait filiereId au lieu de promotionId
- **Solution**: Iteration sur les promotions de la filiere
- **Impact**: Les rapports par filiere fonctionnent

### 5. Export PDF
- **Probleme**: iText 5 (licence commerciale) + moyenne non ponderee
- **Solution**: OpenPDF (Apache 2.0) + moyenne ponderee par coefficient
- **Impact**: Export legal et correct

### 6. Import Excel
- **Probleme**: Support uniquement .xlsx
- **Solution**: Support .xls et .xlsx via Apache POI
- **Impact**: Plus de compatibilite

### 7. Validation
- **Probleme**: Regex CNE trop restrictive, pas de validation telephone
- **Solution**: Regex ameliorees pour CNE, email, telephone
- **Impact**: Validation robuste

---

## Fonctionnalites Ajoutees (manquantes dans l'original)

### Responsable Planning
1. **Gestion des Promotions** (nouveau)
   - Ajouter/Supprimer des promotions
   - Associer filiere + annee

2. **Gestion des Filieres** (nouveau)
   - Ajouter/Modifier/Supprimer des filieres

3. **Gestion des Modules/Sous-Modules** (ameliore)
   - Interface complete avec arborescence
   - Assignation des enseignants aux sous-modules

4. **Gestion des Notes** (nouveau)
   - Saisie manuelle des notes par promotion/module/sous-module
   - Validation administrative des notes
   - Vue complete des notes par promotion

5. **Statistiques** (complet)
   - Taux de reussite par promotion
   - Meilleur etudiant
   - Classement complet

6. **Rapports** (complet)
   - Rapport par promotion (PDF)
   - Rapport par filiere (PDF)

### Enseignant
1. **Vue des Sous-Modules assignes**
2. **Saisie des notes** (manuelle + Excel)
3. **Moyenne de classe** en temps reel
4. **Historique des notes saisies**

### Responsable Filiere
1. **Vue des etudiants** de sa filiere
2. **Vue des notes** de sa filiere
3. **Classement** de sa filiere
4. **Export PDF** du classement

---

## Structure du Projet

```
gestion_ecole_improved/
├── pom.xml                          # Maven - dependencies modernes
├── src/main/
│   ├── java/
│   │   ├── config/
│   │   │   ├── AppConfig.java       # Chargement config.properties
│   │   │   ├── Constantes.java      # Toutes les constantes
│   │   │   ├── DatabaseConnection.java  # HikariCP pool
│   │   │   └── ServiceLocator.java  # DI container
│   │   ├── controller/
│   │   │   ├── DashboardController.java   # Interface commune
│   │   │   ├── LoginController.java       # Connexion avec BCrypt
│   │   │   ├── ResponsableController.java # Dashboard complet admin
│   │   │   ├── EnseignantController.java  # Dashboard enseignant
│   │   │   ├── ResponsableFiliereController.java
│   │   │   └── EtudiantDialogController.java
│   │   ├── dao/
│   │   │   ├── EtudiantDAO.java
│   │   │   ├── FiliereDAO.java
│   │   │   ├── PromotionDAO.java      # NOUVEAU
│   │   │   ├── ModuleDAO.java
│   │   │   ├── NoteDAO.java
│   │   │   ├── UtilisateurDAO.java
│   │   │   └── StatistiqueDAO.java
│   │   ├── exception/
│   │   │   ├── AuthException.java
│   │   │   ├── DAOException.java
│   │   │   ├── ServiceException.java
│   │   │   └── ValidationException.java
│   │   ├── ma/ecole/
│   │   │   └── MainApp.java
│   │   ├── model/
│   │   │   ├── Utilisateur.java      # Enum Role, equals/hashCode
│   │   │   ├── Etudiant.java         # Enum Statut
│   │   │   ├── Enseignant.java       # Enum Grade
│   │   │   ├── ResponsablePlanning.java
│   │   │   ├── ResponsableFiliere.java
│   │   │   ├── Filiere.java
│   │   │   ├── Promotion.java        # NOUVEAU
│   │   │   ├── Module.java
│   │   │   ├── SousModule.java
│   │   │   └── Note.java             # Enum TypeNote, validee
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── EtudiantService.java
│   │   │   ├── FiliereService.java
│   │   │   ├── PromotionService.java  # NOUVEAU
│   │   │   ├── ModuleService.java
│   │   │   ├── NoteService.java
│   │   │   └── StatistiqueService.java
│   │   ├── service/impl/
│   │   │   ├── AuthServiceImpl.java      # BCrypt
│   │   │   ├── EtudiantServiceImpl.java  # Validation complete
│   │   │   ├── FiliereServiceImpl.java
│   │   │   ├── PromotionServiceImpl.java # NOUVEAU
│   │   │   ├── ModuleServiceImpl.java
│   │   │   ├── NoteServiceImpl.java
│   │   │   └── StatistiqueServiceImpl.java # Bug fixe
│   │   └── util/
│   │       ├── Validator.java        # Regex ameliorees
│   │       ├── FxUtils.java          # Helpers UI
│   │       ├── ExcelImporter.java    # .xls + .xlsx
│   │       └── PDFExporter.java      # OpenPDF, moyenne ponderee
│   └── resources/
│       ├── config.properties
│       ├── css/style.css             # Theme moderne
│       ├── view/
│       │   ├── LoginView.fxml        # Design moderne
│       │   ├── ResponsableView.fxml  # 6 onglets complets
│       │   ├── EnseignantView.fxml   # Vue enseignant complete
│       │   ├── ResponsableFiliereView.fxml
│       │   └── dialog/
│       │       └── EtudiantDialog.fxml
│       └── sql/
│           └── schema.sql            # Schema complet + vues
```

---

## Installation

### 1. Base de donnees
```bash
mysql -u root -p < src/main/resources/sql/schema.sql
```

### 2. Configuration
Editer `src/main/resources/config.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/gestion_ecole?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Africa/Casablanca
db.user=root
db.password=votre_mot_de_passe
```

### 3. Compilation
```bash
mvn clean install
```

### 4. Execution
```bash
mvn javafx:run
```

---

## Utilisateurs de test (a creer avec BCrypt)

Generer un hash BCrypt:
```java
import org.mindrot.jbcrypt.BCrypt;
String hash = BCrypt.hashpw("admin123", BCrypt.gensalt(12));
```

Exemple d'insertion:
```sql
INSERT INTO utilisateur (login, mot_de_passe, nom, prenom, email, role, actif) 
VALUES ('admin', '$2a$12$...hash...', 'Admin', 'System', 'admin@ecole.ma', 'RESPONSABLE_PLANNING', TRUE);
```

---

## Ameliorations Futures
- [ ] Tests unitaires (JUnit 5)
- [ ] Internationalisation (i18n)
- [ ] Export Excel des rapports
- [ ] Graphiques (JavaFX Charts)
- [ ] Notifications email
- [ ] Backup/Restore BD
- [ ] Audit log
- [ ] Dark mode toggle
