# DevisAI BTP

> Transforme automatiquement les e-mails clients BTP en devis structurés grâce à l'IA.

Le commercial reçoit un e-mail vague — *"Bonjour, j'aurais besoin de quelques sacs de ciment et du sable, pas trop cher, pour fin du mois"* — et l'application génère en quelques secondes un devis pré-rempli avec les articles, les prix du catalogue, les alertes et les incohérences détectées. Il n'a plus qu'à vérifier et valider.

---

## Pipeline

```
E-mail reçu
    │
    ▼
[Nettoyage IA]  ──  Supprime signatures, disclaimers, réponses précédentes
    │
    ▼
[Extraction GPT-4o-mini]  ──  Détecte produits, quantités, budget, date de livraison, client
    │
    ▼
[Analyse & Tarification]  ──  Catégorise les articles, applique le catalogue BTP (éco/standard/premium)
    │
    ▼
[Génération du brouillon]  ──  Calcul HT/TVA/TTC, alertes quantités floues, vérification budget
    │
    ▼
[Validation commerciale]  ──  Éditeur React : modification des champs, ajout/suppression d'articles
    │
    ▼
[Export PDF]  ──  Devis professionnel prêt à envoyer au client
```

---

## Fonctionnalités

- **Scan automatique des e-mails** — lecture IMAP Gmail toutes les 5 minutes, ou déclenchement manuel
- **Extraction IA** — GPT-4o-mini extrait produits, quantités, budget, date, nom du client (y compris depuis la signature)
- **Alertes intelligentes** — quantités floues ("quelques", "une dizaine"), budget dépassé, date dans le passé
- **Éditeur de devis** — tous les champs modifiables : articles, prix, remises, client, priorité, date de livraison
- **Export PDF** — devis professionnel généré côté serveur avec OpenPDF
- **Dashboard** — statistiques en temps réel : total devis, montant moyen, taux de validation, graphiques par statut
- **Feedback loop** — enregistre les corrections des commerciaux et calcule le taux de précision de l'IA
- **Historique & Clients** — recherche par client, filtre par statut et par date, fiche client avec tous ses devis

---

## Stack technique

| Côté | Technologies |
|------|-------------|
| **Backend** | Java 17 · Spring Boot 3.2.6 · Spring Data JPA · H2 (fichier) · OpenPDF · JavaMail (IMAP) |
| **IA** | OpenAI GPT-4o-mini · Structured Outputs · Prompt engineering |
| **Frontend** | React 19 · TypeScript · Vite · Ant Design · Recharts |
| **Tests** | JUnit 5 · Mockito · @DataJpaTest · @WebMvcTest · Vitest · Testing Library |
| **API** | REST · Swagger UI (SpringDoc OpenAPI) |

---

## Lancer le projet en local

### Prérequis

- Java 17+
- Maven 3.8+
- Node.js 18+
- Une clé API OpenAI

### 1. Cloner le projet

```bash
git clone https://github.com/faten-M/projet-devis.git
cd projet-devis
```

### 2. Configurer les variables d'environnement

```bash
# Windows
set OPENAI_API_KEY=sk-...

# Mac/Linux
export OPENAI_API_KEY=sk-...
```

Pour activer la lecture automatique des e-mails (optionnel) :

```bash
set EMAIL_USERNAME=votre-adresse@gmail.com
set EMAIL_PASSWORD=votre-app-password-gmail
```

### 3. Lancer le backend

```bash
mvn spring-boot:run
```

Le serveur démarre sur `http://localhost:8080`  
Swagger UI disponible sur `http://localhost:8080/swagger-ui.html`

### 4. Lancer le frontend

```bash
cd frontend
npm install
npm run dev
```

L'application est disponible sur `http://localhost:5173`

---

## Déploiement (Railway)

L'application se déploie sur [Railway](https://railway.app) en tant que service unique (Spring Boot sert à la fois l'API et le frontend React compilé).

**Variables d'environnement à configurer dans Railway :**

| Variable | Description |
|----------|-------------|
| `OPENAI_API_KEY` | Clé API OpenAI |
| `EMAIL_IMAP_ENABLED` | `true` pour activer le scan automatique |
| `EMAIL_USERNAME` | Adresse Gmail |
| `EMAIL_PASSWORD` | App Password Gmail |
| `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` | Fournis automatiquement par Railway si tu ajoutes un service PostgreSQL |

---

## Tests

```bash
mvn test          # 64 tests backend (39 unitaires + 10 intégration + 15 controllers)
cd frontend && npm run test   # tests frontend
```

---

## Structure du projet

```
projet-devis/
├── src/main/java/com/projetdevis/
│   ├── controller/     # API REST (Devis, Stats, Admin, PDF, Clients, Produits)
│   ├── service/        # Pipeline IA (nettoyage, extraction, analyse, brouillon, PDF)
│   ├── model/          # Entités JPA (DraftQuote, QuoteItem, Client, CorrectionIA...)
│   ├── repository/     # Spring Data JPA
│   └── dto/            # Objets de transfert (request/response)
├── src/test/           # Tests unitaires et d'intégration
└── frontend/
    └── src/
        ├── pages/      # InboxPage, DevisEditorPage, DashboardPage, HistoriquePage
        └── types/      # Interfaces TypeScript
```

---

## Ce que j'ai appris

Ce projet m'a permis de :
- Concevoir et implémenter un **pipeline IA bout en bout** en production (nettoyage → extraction → analyse → brouillon)
- Utiliser l'API **OpenAI Structured Outputs** pour garantir des réponses JSON valides
- Construire une **API REST complète** avec Spring Boot et documenter automatiquement avec Swagger
- Créer une **interface React/TypeScript** professionnelle avec Ant Design
- Mettre en place un **feedback loop** pour mesurer et améliorer la qualité des extractions IA
- Appliquer les bonnes pratiques : tests unitaires (64 tests backend), variables d'environnement pour les secrets, séparation des responsabilités

---

*Projet de stage — Soutenance juillet 2026*
