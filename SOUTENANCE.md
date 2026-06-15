# Guide de soutenance — DevisAI BTP
# À lire avant la présentation du 7 juillet 2026

---

## C'EST QUOI MON PROJET EN 1 PHRASE

> J'ai créé une application qui lit automatiquement les emails de clients BTP
> et génère des devis structurés grâce à l'intelligence artificielle GPT-4o-mini.

---

## LE PIPELINE — CE QUE JE DIS AU JURY

"Voici comment fonctionne l'application étape par étape :"

```
1. Un client envoie un email  →  "Bonjour, j'ai besoin de 200 sacs de ciment"

2. L'application lit l'email automatiquement (toutes les 5 minutes via IMAP)
   ou manuellement si je clique "Scanner les emails"

3. L'IA nettoie l'email (supprime les signatures, les anciens messages)

4. L'IA extrait les informations :
   - Les articles (ciment, sable, gravier...)
   - Les quantités (200 sacs)
   - Le budget du client
   - La date de livraison souhaitée
   - Le nom du client

5. L'application génère un brouillon de devis avec les prix du catalogue BTP

6. Le commercial vérifie, corrige si besoin, et valide

7. Le PDF est généré et prêt à envoyer au client
```

---

## LES TECHNOLOGIES — CE QUE C'EST SIMPLEMENT

### Spring Boot (le backend)
C'est le serveur Java. Il gère :
- Les routes API (/api/devis, /api/stats...)
- La connexion à la base de données
- L'appel à OpenAI
- La génération du PDF
- La lecture des emails

### React + TypeScript (le frontend)
C'est l'interface que l'utilisateur voit dans le navigateur.
React affiche les devis, les graphiques, les formulaires.
TypeScript c'est JavaScript avec des types (moins d'erreurs).

### H2 (la base de données en local)
C'est une base de données légère qui vit dans un fichier sur le PC.
Elle stocke les devis, les clients, les articles, les corrections.
Quand tu fermes l'app, les données restent dans le fichier.
On l'utilise en développement parce qu'elle ne nécessite pas d'installation.

### PostgreSQL (la base de données en production)
C'est exactement la même chose que H2 — même SQL, mêmes tables —
mais c'est un vrai serveur de base de données séparé.
On l'utilise en production parce qu'elle est plus robuste.
La migration de H2 vers PostgreSQL = juste changer la configuration,
pas une ligne de code métier n'a changé.

### OpenAI GPT-4o-mini (l'IA)
C'est le modèle d'intelligence artificielle d'OpenAI.
Je lui envoie le texte de l'email + des instructions précises (le prompt).
Il me répond avec les informations extraites au format JSON structuré.
J'utilise les "Structured Outputs" d'OpenAI pour garantir un JSON valide.

### Docker (pour le déploiement)
C'est une boîte qui contient l'application + tout ce dont elle a besoin.
Le Dockerfile est la recette pour créer cette boîte.
En production, Railway ouvre cette boîte et fait tourner l'application.
En développement, on n'utilise pas Docker — on lance directement avec Maven.

### Railway (le serveur cloud)
C'est comme un serveur en ligne.
On lui donne le code GitHub, il construit et héberge l'application.
Il fournit une URL publique.
Il fournit aussi une base PostgreSQL gratuite.

### Vite (l'outil de build React)
C'est l'outil qui compile le code React en fichiers HTML/CSS/JS.
En développement : npm run dev → serveur sur localhost:5173
En production : npm run build → crée les fichiers finaux dans dist/

### Ant Design
C'est une bibliothèque de composants visuels pour React.
Les boutons, tableaux, formulaires, graphiques — tout vient de là.
Ça évite de tout coder de zéro.

---

## LES QUESTIONS DU JURY — LES RÉPONSES PRÊTES

### "Comment l'IA sait ce qu'est du ciment ?"
> "Je lui fournis le catalogue BTP dans le prompt — la liste des catégories
> (gros oeuvre, électricité, plomberie...) et les fourchettes de prix.
> Elle classe chaque article et applique la grille tarifaire."

### "Et si l'email est mal écrit ou en anglais ?"
> "L'application est conçue pour traiter des emails en français, ce qui
> correspond au contexte BTP français visé. Je n'ai pas testé d'autres langues
> dans ce projet. GPT-4o-mini est techniquement capable de comprendre plusieurs
> langues, donc le support multilingue serait une évolution possible, mais ce
> n'est pas une fonctionnalité que j'ai développée ni testée."

### "C'est quoi le feedback loop ?"
> "Quand le commercial change une quantité ou un prix dans le devis,
> le système enregistre l'écart entre ce que l'IA avait proposé et ce que
> le commercial a corrigé. Ça me permet d'afficher un taux de précision
> et d'identifier les types d'erreurs récurrentes de l'IA."

### "Pourquoi H2 et pas PostgreSQL ?"
> "H2 en local pour simplifier le développement — pas besoin d'installer
> une base de données séparément. En production sur Railway j'utilise
> PostgreSQL. La migration s'est faite en changeant uniquement la
> configuration Spring Boot, sans toucher une seule ligne de code métier.
> C'est l'avantage de Spring Data JPA — le code est identique pour les
> deux bases de données."

### "Tu as combien de tests ?"
> "64 tests backend automatisés qui passent — 39 tests unitaires sur les services
> avec Mockito, 10 tests d'intégration JPA sur les repositories avec
> @DataJpaTest, et 15 tests de controllers avec @WebMvcTest et MockMvc.
> J'ai aussi des tests frontend avec Vitest."

### "C'est quoi le Dockerfile ?"
> "C'est la recette pour construire l'application en une seule boîte Docker.
> Il fait 3 étapes : d'abord il compile le frontend React, ensuite il le
> copie dans le backend Spring Boot et compile le JAR, enfin il crée une
> image légère qui ne contient que Java et le JAR. Le résultat est un seul
> serveur qui gère à la fois l'API et le frontend."

### "Pourquoi avoir utilisé les Structured Outputs d'OpenAI ?"
> "Sans ça, l'IA pouvait répondre en texte libre ou avec un JSON mal formé.
> Les Structured Outputs garantissent que la réponse respecte exactement
> le schéma Java que j'ai défini. Zéro parsing manuel, zéro exception JSON."

### "Comment tu sécurises les secrets (clé API, mot de passe Gmail) ?"
> "Tous les secrets sont dans des variables d'environnement, jamais dans
> le code. La clé OpenAI c'est OPENAI_API_KEY, le mot de passe Gmail
> c'est EMAIL_PASSWORD. Ces variables sont configurées dans Railway en
> production. Elles n'apparaissent jamais dans le code ni sur GitHub."

### "Tu as utilisé quoi pour générer le PDF ?"
> "La bibliothèque OpenPDF côté serveur Spring Boot.
> Le PDF est généré en Java avec les données du devis et retourné
> directement en réponse HTTP avec le Content-Type application/pdf."

### "C'est quoi IMAP ?"
> "C'est le protocole pour lire les emails depuis un serveur de messagerie.
> Mon application se connecte à Gmail via IMAP toutes les 5 minutes,
> lit les nouveaux emails non lus, et les traite automatiquement.
> C'est comme ce que fait Outlook ou Thunderbird, mais en Java."

---

## LES CHIFFRES À RETENIR

- **64 tests** backend (39 unitaires + 10 intégration + 15 controllers)
- **5 minutes** — fréquence du scan automatique des emails
- **6 services** backend (Pipeline, Extraction, Analyse, Brouillon, PDF, Email)
- **4 pages** frontend (Inbox, Éditeur, Dashboard, Historique)
- **3 étapes** dans le Dockerfile (Node, Maven, JRE)
- **GPT-4o-mini** — le modèle utilisé (pas GPT-4o car moins cher, suffisant pour l'extraction)

---

## L'ORDRE DE LA DÉMO (8 minutes)

```
1. (2 min) Scénario 1 — L'email clair de Thomas Mercier
   → Scanner les emails → 3 devis apparaissent
   → Ouvrir Mercier → montrer extraction automatique → valider → télécharger PDF

2. (3 min) Scénario 2 — L'email vague de Karim Bensalem
   → Montrer les alertes (quantité imprécise, budget dépassé)
   → Corriger une quantité → sauvegarder
   → Aller sur Dashboard → montrer le feedback loop mis à jour

3. (1 min) Scénario 3 — L'email automatique de Sophie Arnaud
   → "Ce devis est arrivé tout seul, l'app surveille la boîte mail toutes les 5 minutes"

4. (1 min) Dashboard
   → Montrer les KPI, graphiques, taux de précision IA

5. (1 min) Questions du jury
```

---

## AVANT LA SOUTENANCE — CHECKLIST

La veille :
- [ ] Lancer mvn test → vérifier que les 77 tests passent
- [ ] Lancer l'app en local et tester les 3 scénarios
- [ ] Envoyer les 3 emails depuis le téléphone 10 min avant

Le matin :
- [ ] Ouvrir les 2 terminaux
- [ ] Vérifier que localhost:5173 s'ouvre
- [ ] Avoir le DEMO.md ouvert sur un deuxième écran

Pendant la démo :
- [ ] Parler fort et lentement
- [ ] Expliquer ce qui se passe pendant que ça charge
- [ ] Ne pas paniquer si une question est difficile — "bonne question,
      je vais expliquer..." puis utiliser les réponses de ce fichier

---

*Soutenance le 7 juillet 2026 — Tu as construit quelque chose de bien, fais confiance à ton travail.*
