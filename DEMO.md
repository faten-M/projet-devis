# Guide de démo — Soutenance DevisAI BTP

Durée totale : ~8 minutes
Ordre : Scénario 1 → Scénario 2 → Scénario 3 → Dashboard

---

## Avant de commencer

Lance les deux serveurs :
```bash
# Terminal 1 — backend
mvn spring-boot:run

# Terminal 2 — frontend
cd frontend && npm run dev
```

Ouvre `http://localhost:5173` dans le navigateur.

---

## Scénario 1 — L'email clair (2 min)

**Ce que tu montres :** L'IA extrait parfaitement un email bien rédigé → devis prêt en 1 clic.

**Ce que tu dis :**
> "Je vais simuler un commercial qui reçoit un email client. Je clique sur 'Tester manuellement', je colle l'email, et l'IA génère le devis."

**Email à coller :**
```
Bonjour,

Je suis responsable de chantier chez Bâtipro SARL et je souhaite obtenir un devis
pour les matériaux suivants :

- 200 sacs de ciment CEM II 32.5
- 50 m³ de sable fin de construction
- 30 m³ de gravier 20/40

Nous avons un budget de 8 000 € HT pour cette commande.
La livraison est souhaitée pour le 15 juillet 2026 au plus tard.

Dans l'attente de votre retour,
Cordialement,
Thomas Mercier
thomas.mercier@batipro.fr
```

**Après génération — ce que tu montres :**
- Le nom "Thomas Mercier" a été détecté automatiquement dans la signature
- Les 3 articles sont là avec les bonnes quantités
- Le budget 8 000 € est renseigné
- La date du 15 juillet est détectée
- Tu cliques "Valider (Prêt)" → le devis passe au statut Prêt
- Tu cliques "Télécharger le PDF" → le PDF s'ouvre

**Ce que tu dis :**
> "En 30 secondes, le commercial a un devis complet. Sans l'IA, il aurait dû saisir tout ça manuellement."

---

## Scénario 2 — L'email vague (3 min)

**Ce que tu montres :** L'IA détecte les ambiguïtés et alerte le commercial → le commercial corrige → le feedback loop enregistre.

**Ce que tu dis :**
> "Maintenant un cas plus réaliste — les clients écrivent rarement des emails parfaits."

**Email à coller :**
```
Bonjour,

J'ai un petit chantier de rénovation et j'aurais besoin de quelques sacs de ciment,
une dizaine de parpaings environ, et si possible des rails pour cloisons sèches.
Je voudrais pas dépenser trop, mettons 500 euros maximum.
Ce serait bien pour bientôt si possible.

Merci,
Karim Bensalem
```

**Après génération — ce que tu montres :**

1. **Les alertes en orange** dans le panneau de droite :
   - *"Quantité imprécise — 'quelques' interprétée comme 3 — à confirmer avec le client"*
   - *"Quantité imprécise — 'une dizaine' interprétée comme 10 — à confirmer avec le client"*

2. **Le budget dépassé** — l'IA a mis 500 € mais le total dépasse → alerte en rouge

3. **Le commercial corrige :**
   - Change "3" → "20" pour les sacs de ciment
   - Change la date de livraison
   - Clique "Sauvegarder"

4. **Va sur le Dashboard** → la carte "Feedback loop" montre :
   - Corrections enregistrées : +1
   - Précision IA : le score se met à jour

**Ce que tu dis :**
> "L'IA ne rejette pas l'email — elle fait de son mieux et signale ce qu'elle n'est pas sûre. Le commercial a toutes les informations pour contacter le client et corriger. Et chaque correction est enregistrée pour mesurer la précision de l'IA dans le temps."

---

## Scénario 3 — L'email automatique (2 min)

**Ce que tu montres :** Un email arrive dans Gmail → il apparaît tout seul dans l'inbox sans rien faire.

**Ce que tu dis :**
> "Le troisième scénario, c'est celui qui impressionne le plus : le commercial n'a même pas besoin d'être là."

**Étapes :**
1. Envoie un email à l'adresse Gmail configurée depuis ton téléphone ou un autre compte
2. Clique sur "Scanner les emails" dans l'inbox
3. Le devis apparaît dans la liste automatiquement

**Email à envoyer depuis le téléphone :**
```
Bonjour,

Pour un chantier à Marseille, je cherche :
- 100 kg de mortier colle
- 50 m² de carrelage sol gris 60x60
- 2 rouleaux de bande à joint

Budget : 1200 euros, livraison souhaitée fin juin.

Cordialement,
Sophie Arnaud
sophie.arnaud@renovation13.fr
```

**Ce que tu dis :**
> "L'application surveille la boîte mail toutes les 5 minutes. Dès qu'un email arrive, il est traité automatiquement. Le commercial arrive le matin et trouve ses devis déjà générés."

---

## Dashboard — La cerise sur le gâteau (1 min)

**Ce que tu montres :**
- Les KPI : total devis, montant moyen, taux de validation
- Le graphique de répartition par statut
- La carte "Feedback loop" avec le taux de précision IA

**Ce que tu dis :**
> "Le manager peut suivre l'activité en temps réel. Et ce taux de précision — ici 88% — c'est une métrique qu'on ne peut obtenir qu'avec un système qui apprend des corrections humaines. Plus on utilise l'application, plus ce chiffre s'améliore."

---

## Si on te pose des questions

**"Comment l'IA sait ce qu'est du ciment ?"**
> "Je lui fournis le catalogue BTP dans le prompt. Elle catégorise chaque article et applique la grille tarifaire correspondante : économique, standard ou premium."

**"Et si l'email est en anglais ou mal écrit ?"**
> "GPT-4o-mini comprend les emails mal écrits, avec des fautes, en plusieurs langues. C'est un de ses points forts par rapport à une extraction par mots-clés classique."

**"C'est quoi le feedback loop concrètement ?"**
> "À chaque fois qu'un commercial change une quantité ou un prix dans un devis, le système enregistre : valeur IA vs valeur corrigée. Ça permet de calculer un taux de précision et d'identifier les champs que l'IA rate souvent — comme les quantités floues."

**"Pourquoi H2 et pas PostgreSQL ?"**
> "H2 en mode fichier pour simplifier le développement local. En production avec Railway ou Render, on brancherait PostgreSQL — la migration est transparente avec Spring Data JPA."

---

## Ordre recommandé sur les slides

1. Le problème (les commerciaux perdent du temps à saisir des devis)
2. La solution (pipeline IA : email → devis en 30 secondes)
3. Démo live scénario 1
4. Démo live scénario 2 (alertes + feedback)
5. Dashboard
6. Stack technique (1 slide)
7. Ce que j'ai appris / perspectives

---

*Entraîne-toi 2-3 fois avant la soutenance pour que la démo soit fluide.*
