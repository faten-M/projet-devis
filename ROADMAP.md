# Roadmap du projet — De "ça marche en local" à "regarde mon app en ligne"

Ce fichier décrit toutes les étapes pour transformer le projet en un produit complet, déployé, et prêt à montrer en entretien.

Chaque étape est pensée pour être faite dans l'ordre. Ne saute pas d'étape. Prends le temps de bien finir une étape avant de passer à la suivante.

---

## Phase 1 — Remettre la maison en ordre (avant de construire la suite)

Avant d'ajouter quoi que ce soit, on s'assure que ce qui existe est propre et solide.

---

### Étape 1.1 — Corriger les tests qui plantent

**Où :** `src/test/java/com/projetdevis/service/ExtractionServiceTest.java`

**Le problème :** Quand tu lances les tests avec `mvn test`, 2 tests sur 18 échouent. Ils échouent parce que `ExtractionService` crée un objet `ExtractInfoIA` dans son constructeur, et cet objet a besoin d'une clé API OpenAI pour fonctionner. Dans un test, on ne veut pas appeler la vraie API — on veut simuler (mocker).

**Ce qu'il faut faire :**

1. Ouvre `ExtractionService.java`
2. Trouve le constructeur. Dedans, tu verras quelque chose comme `this.quantityIA = new ExtractInfoIA();`
3. Ajoute un **deuxième constructeur** qui accepte un `ExtractInfoIA` en paramètre, comme ça :

```java
// Constructeur normal (utilisé par l'application)
public ExtractionService() {
    this.quantityIA = new ExtractInfoIA();
}

// Constructeur pour les tests (on peut lui passer un faux ExtractInfoIA)
public ExtractionService(ExtractInfoIA quantityIA) {
    this.quantityIA = quantityIA;
}
```

4. Dans le fichier de test `ExtractionServiceTest.java`, utilise Mockito pour créer un faux `ExtractInfoIA` et passe-le au nouveau constructeur. Regarde comment c'est fait dans `EmailCleanerIATest.java` — c'est exactement le même principe.

**Comment vérifier que c'est bon :** Lance `mvn test`. Les 18 tests doivent passer au vert.

---

### Étape 1.2 — Ajouter quelques tests sur les services métier

**Le problème :** Aujourd'hui, seuls les composants IA ont des tests. Les 4 services principaux (`AnalysisService`, `DraftService`, `ValidationService`, `CrmService`) n'en ont aucun. C'est un signal négatif pour un recruteur qui regarde le code.

**Ce qu'il faut faire :**

Pour chaque service, écris 3 à 5 tests qui vérifient les cas importants. Pas besoin de tout tester — juste les comportements clés.

Exemples de tests utiles :
- `AnalysisService` : est-ce qu'un article "bureau en chêne" est bien catégorisé en "Bureau" ?
- `DraftService` : est-ce qu'un devis avec 3 articles calcule bien le total HT et TTC ?
- `ValidationService` : est-ce qu'une remise de 10% est bien appliquée sur le montant ?
- `CrmService` : est-ce qu'on extrait bien le nom et l'email depuis un texte d'email ?

**Comment vérifier que c'est bon :** `mvn test` passe, et tu as au moins 30+ tests au total.

---

## Phase 2 — Construire l'API (le cerveau accessible de l'extérieur)

Pour l'instant, le pipeline ne tourne que dans `App.java` avec du texte écrit en dur. On va le rendre accessible via des requêtes HTTP, comme une vraie application.

---

### Étape 2.1 — Ajouter Spring Boot au projet

**C'est quoi Spring Boot ?** C'est un framework Java qui permet de créer une application web très facilement. Il va nous servir à exposer notre pipeline sous forme d'API REST.

**Ce qu'il faut faire :**

1. Dans le fichier `pom.xml`, ajoute la dépendance Spring Boot (parent + starter-web). Cherche "Spring Boot getting started Maven" dans la doc officielle si besoin.
2. Crée une classe principale annotée `@SpringBootApplication`
3. Vérifie que l'application démarre avec `mvn spring-boot:run` — tu dois voir un message du genre "Started application on port 8080"

**Comment vérifier que c'est bon :** Ouvre ton navigateur et va sur `http://localhost:8080`. Tu dois voir une page (même une erreur 404, c'est normal à ce stade — ça veut dire que le serveur tourne).

---

### Étape 2.2 — Créer l'endpoint principal : soumettre un email

**C'est quoi un endpoint ?** C'est une URL sur ton serveur qui accepte des requêtes. Par exemple, quand tu envoies un email au serveur, il te renvoie un devis.

**Ce qu'il faut faire :**

Crée un contrôleur (`@RestController`) avec une route POST, par exemple `POST /api/devis`. Cette route :

1. Reçoit le texte d'un email dans le corps de la requête
2. Fait passer cet email dans le pipeline complet (nettoyage → extraction → analyse → génération de devis)
3. Renvoie le devis brouillon en JSON

```
POST /api/devis
Body : { "email": "Bonjour, je souhaite commander 5 bureaux en chêne..." }
Réponse : { "articles": [...], "totalHT": 2500, "totalTTC": 3000, ... }
```

**Comment vérifier que c'est bon :** Utilise un outil comme Postman (application gratuite) ou la commande `curl` dans le terminal pour envoyer un email de test. Tu dois recevoir un devis en retour.

---

### Étape 2.3 — Ajouter la base de données

**Pourquoi ?** Pour l'instant, rien n'est sauvegardé. Quand l'application s'arrête, tout disparaît. On veut garder en mémoire : les devis générés, les clients, et le catalogue de produits.

**Ce qu'il faut faire :**

1. Ajoute la dépendance Spring Data JPA + PostgreSQL (ou H2 pour commencer — c'est une base de données embarquée, plus simple pour démarrer)
2. Transforme tes modèles existants (`DraftQuote`, `Client`, etc.) en entités JPA en ajoutant les annotations `@Entity`, `@Id`, etc.
3. Crée des Repository pour chaque entité (ce sont des interfaces qui gèrent automatiquement les opérations en base)
4. Crée une table `produits` avec un catalogue (nom, catégorie, prix unitaire). Les prix ne seront plus en dur dans le code — ils viendront de la base.

**Comment vérifier que c'est bon :** Redémarre l'application, soumets un email, puis arrête et redémarre l'application. Le devis doit toujours être là (requête GET).

---

### Étape 2.4 — Ajouter les autres endpoints

**Ce qu'il faut faire :**

Crée les routes suivantes :

| Méthode | URL | Ce que ça fait |
|---------|-----|----------------|
| `GET` | `/api/devis` | Liste tous les devis générés |
| `GET` | `/api/devis/{id}` | Affiche le détail d'un devis |
| `PUT` | `/api/devis/{id}/valider` | Le commercial valide/modifie un devis |
| `GET` | `/api/clients` | Liste les clients extraits |
| `GET` | `/api/produits` | Liste le catalogue de produits |
| `POST` | `/api/produits` | Ajouter un produit au catalogue |
| `GET` | `/api/stats` | Statistiques globales (nombre de devis, montant moyen, etc.) |

**Comment vérifier que c'est bon :** Chaque route renvoie une réponse JSON correcte. Teste-les une par une avec Postman.

---

### Étape 2.5 — Ajouter Swagger UI

**C'est quoi ?** C'est une page web auto-générée qui documente ton API et permet de la tester directement depuis le navigateur. C'est très impressionnant en démo.

**Ce qu'il faut faire :**

1. Ajoute la dépendance `springdoc-openapi-starter-webmvc-ui` dans le `pom.xml`
2. C'est tout. Sérieusement. Spring Boot fait le reste automatiquement.

**Comment vérifier que c'est bon :** Va sur `http://localhost:8080/swagger-ui.html`. Tu dois voir tous tes endpoints listés avec la possibilité de les tester.

---

## Phase 3 — Construire l'interface web (ce que l'utilisateur voit)

C'est la partie visible. Celle qu'on montre en démo. Celle qui fait dire "ah ouais quand même".

---

### Étape 3.1 — Initialiser le projet front-end

**Ce qu'il faut faire :**

1. Dans un dossier `frontend/` à la racine du projet, crée une application React avec TypeScript : `npx create-react-app frontend --template typescript` (ou Vite, c'est plus rapide : `npm create vite@latest frontend -- --template react-ts`)
2. Installe une bibliothèque de composants pour avoir un rendu pro sans galérer en CSS. Par exemple Ant Design (`npm install antd`) ou Shadcn.
3. Vérifie que l'app démarre : `npm start` → tu vois une page dans le navigateur

**Comment vérifier que c'est bon :** `http://localhost:3000` (ou 5173 avec Vite) affiche la page d'accueil par défaut.

---

### Étape 3.2 — L'écran Inbox (liste des demandes)

C'est la première page que le commercial voit quand il ouvre l'application.

**Ce que l'écran affiche :**
- Une liste des emails reçus / devis générés
- Pour chaque ligne : la date, le nom du client (si détecté), un aperçu du besoin, le statut (nouveau / brouillon / validé / envoyé)
- Un bouton "Nouvelle demande" pour coller un email manuellement
- Un clic sur une ligne ouvre le détail du devis

**Ce qu'il faut faire :**
1. Crée un composant `InboxPage`
2. Au chargement, appelle `GET /api/devis` pour récupérer la liste
3. Affiche les résultats dans un tableau

---

### Étape 3.3 — L'écran Éditeur de devis

C'est le coeur de l'application. Le commercial voit le devis pré-rempli par l'IA et peut le modifier.

**Ce que l'écran affiche :**
- En haut : les infos client (nom, entreprise, email)
- Au milieu : le tableau des articles (désignation, quantité, prix unitaire, total ligne) — chaque cellule est modifiable
- Des boutons pour ajouter/supprimer un article
- Un champ pour appliquer une remise
- En bas : le récapitulatif (total HT, remise, TVA, total TTC)
- Deux boutons : "Valider le devis" et "Rejeter"
- Sur le côté : les alertes (incohérences détectées, infos manquantes)

**Ce qu'il faut faire :**
1. Crée un composant `DevisEditorPage`
2. Appelle `GET /api/devis/{id}` pour charger le devis
3. Affiche un formulaire éditable
4. Le bouton "Valider" appelle `PUT /api/devis/{id}/valider`

---

### Étape 3.4 — L'écran Historique et clients

**Ce que l'écran affiche :**
- Un onglet "Historique" : tous les devis passés avec filtres (par date, par statut, par client)
- Un onglet "Clients" : la liste des clients avec leurs devis associés

---

### Étape 3.5 — Export PDF

Quand un devis est validé, le commercial doit pouvoir télécharger un PDF propre à envoyer au client.

**Ce qu'il faut faire :**

Côté back-end :
1. Ajoute une librairie de génération de PDF (par exemple iText ou OpenPDF pour Java)
2. Crée un endpoint `GET /api/devis/{id}/pdf` qui génère et renvoie le fichier PDF
3. Le PDF doit contenir : les infos de l'entreprise, les infos client, le tableau des articles, les totaux, les conditions

Côté front-end :
1. Ajoute un bouton "Télécharger le PDF" sur l'écran de l'éditeur de devis
2. Ce bouton appelle l'endpoint et télécharge le fichier

---

## Phase 4 — Connexion email (le projet devient autonome)

Jusqu'ici, le commercial devait copier-coller l'email. Maintenant, l'application va chercher les emails toute seule.

---

### Étape 4.1 — Lire les emails automatiquement

**Ce qu'il faut faire :**

1. Utilise la librairie JavaMail (jakarta.mail) pour te connecter à une boîte mail en IMAP
2. Crée un service `EmailReaderService` qui :
   - Se connecte à la boîte mail (identifiants dans un fichier de configuration)
   - Lit les emails non lus
   - Pour chaque email, lance le pipeline automatiquement
   - Marque l'email comme lu
3. Planifie ce service pour qu'il tourne toutes les 5 minutes (avec `@Scheduled` de Spring)

**Pour tester sans risque :** Crée une adresse Gmail dédiée au projet. Active l'accès IMAP dans les paramètres. Envoie-toi des emails de test.

**Comment vérifier que c'est bon :** Envoie un email à l'adresse configurée. Attends 5 minutes. Le devis apparaît dans l'inbox de l'application.

---

## Phase 5 — Le dashboard et l'intelligence (l'effet wahou)

---

### Étape 5.1 — Le tableau de bord

**Ce que l'écran affiche :**
- Nombre total de devis (ce mois, cette semaine)
- Montant moyen des devis
- Taux de validation (combien de brouillons deviennent des devis validés)
- Temps moyen entre réception de l'email et validation du devis
- Un graphique simple (barres ou courbes) montrant l'évolution dans le temps

**Ce qu'il faut faire :**
1. L'endpoint `GET /api/stats` calcule ces chiffres à partir de la base de données
2. Côté front, utilise une librairie de graphiques (Recharts ou Chart.js) pour les afficher

---

### Étape 5.2 — Le feedback loop (apprentissage des corrections)

**L'idée :** Quand un commercial corrige un devis (change un prix, corrige une quantité mal extraite, re-catégorise un article), on enregistre cette correction. Ces corrections servent ensuite à améliorer les futures extractions.

**Ce qu'il faut faire :**

1. Quand le commercial modifie un devis, stocke en base : la valeur originale (ce que l'IA avait mis) et la valeur corrigée (ce que le commercial a mis)
2. Crée un endpoint `GET /api/corrections` qui liste toutes les corrections passées
3. Utilise ces corrections pour ajuster les règles d'extraction. Par exemple :
   - Si le commercial change souvent la catégorie d'un produit, on adapte les mots-clés
   - Si le prix estimé est toujours corrigé à la hausse, on ajuste la grille
4. Affiche sur le dashboard : "Précision de l'extraction : X%" (calculé en comparant les valeurs originales aux valeurs finales)

Même une version simple de cette mécanique est très impressionnante en entretien.

---

## Phase 6 — Déploiement et finitions

---

### Étape 6.1 — Déployer l'application en ligne

**Pourquoi :** Pour pouvoir dire "voici le lien, testez vous-même". Ça change tout.

**Ce qu'il faut faire :**

1. Crée un `Dockerfile` qui build l'application Java et sert le front-end
2. Choisis une plateforme de déploiement gratuite :
   - **Railway** (le plus simple) : connecte ton repo GitHub, Railway détecte le Dockerfile et déploie automatiquement
   - **Render** : même principe
   - **Fly.io** : un peu plus technique, mais très fiable
3. Configure une base de données PostgreSQL sur la même plateforme (elles proposent toutes des bases gratuites)
4. Configure les variables d'environnement (clé API OpenAI, identifiants mail, etc.)

**Comment vérifier que c'est bon :** Tu as une URL publique (genre `https://projet-devis.up.railway.app`) qui affiche ton application et qui fonctionne.

---

### Étape 6.2 — Refaire le README

Le README est la première chose qu'un recruteur voit sur GitHub. Il doit donner envie.

**Ce qu'il doit contenir :**
1. **Une phrase d'accroche** — ce que fait le projet, en une ligne
2. **Un screenshot ou GIF** de l'application en fonctionnement
3. **Le lien vers l'app déployée**
4. **Un schéma du pipeline** (un diagramme simple avec des flèches : Email → Nettoyage IA → Extraction → Analyse → Devis → Validation → CRM)
5. **La stack technique** (Java 17, Spring Boot, React, TypeScript, PostgreSQL, OpenAI API)
6. **Comment lancer le projet en local** (en 3-4 commandes max)
7. **Ce que tu as appris** (optionnel mais les recruteurs adorent ça)

---

### Étape 6.3 — Préparer la démo

**Ce qu'il faut faire :**

Prépare 3 scénarios de démo que tu peux dérouler en 5 minutes :

1. **Le cas simple** : un email clair → le devis se génère parfaitement → validation en un clic → PDF
2. **Le cas intéressant** : un email vague ("je voudrais quelques bureaux, pas trop cher") → l'IA interprète → le commercial ajuste → le système apprend
3. **Le cas automatique** : un email arrive dans la boîte mail → il apparaît tout seul dans l'inbox → le devis est déjà prêt

Entraîne-toi à raconter l'histoire du projet : le problème (les commerciaux perdent du temps), la solution (l'IA pré-remplit les devis), le résultat (gain de temps + moins d'erreurs).

---

## Récap visuel

```
Mars          Avril              Mai                 Juin
─────────────────────────────────────────────────────────────
[Phase 1]     [Phase 2]          [Phase 3]           [Phase 6]
 Tests         API REST           Interface web        Déploiement
 Merge         Base de données    Éditeur de devis     README
 Nettoyage     Swagger            Export PDF           Démo

                                  [Phase 4]           [Phase 5]
                                   Connexion email     Dashboard
                                                       Feedback loop
```

Chaque fin de phase = quelque chose de montrable. Ne passe à la phase suivante que quand la précédente est terminée et testée.
