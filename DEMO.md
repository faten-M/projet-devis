# Guide de démo — Soutenance DevisAI BTP

Durée totale : ~8 minutes
Ordre : Scénario 1 → Scénario 2 → Scénario 3 → Dashboard

---

## Avant de commencer (la veille ou le matin)

Lance les deux serveurs :
```bash
# Terminal 1 — backend
mvn spring-boot:run

# Terminal 2 — frontend
cd frontend && npm run dev
```

Ouvre `http://localhost:5173` dans le navigateur.

---

## 10 minutes AVANT la soutenance

Envoie les 3 emails ci-dessous depuis ton téléphone à ton adresse Gmail configurée.
Comme ça, pendant la démo tu cliques juste "Scanner les emails" et tout apparaît d'un coup.

---

### Email 1 — L'email clair (à envoyer depuis ton téléphone)

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

---

### Email 2 — L'email vague (à envoyer depuis ton téléphone)

```
Bonjour,

J'ai un petit chantier de rénovation et j'aurais besoin de quelques sacs de ciment,
une dizaine de parpaings environ, et si possible des rails pour cloisons sèches.
Je voudrais pas dépenser trop, mettons 500 euros maximum.
Ce serait bien pour bientôt si possible.

Merci,
Karim Bensalem
```

---

### Email 3 — L'email automatique (à envoyer depuis ton téléphone)

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

---

## Scénario 1 — L'email clair (2 min)

**Ce que tu dis pour introduire :**
> "Je vais vous montrer comment fonctionne l'application. Un client a envoyé un email — je clique sur Scanner les emails, et l'IA traite automatiquement tous les emails reçus."

**Tu cliques "Scanner les emails"** → les 3 devis apparaissent dans la liste.

**Tu ouvres le devis de Thomas Mercier et tu montres :**
- Le nom "Thomas Mercier" détecté automatiquement depuis la signature
- Les 3 articles avec les bonnes quantités (200, 50, 30)
- Le budget 8 000 € renseigné
- La date du 15 juillet détectée
- Tu cliques "Valider (Prêt)" → statut passe à Prêt
- Tu cliques "Télécharger le PDF" → le PDF s'ouvre

**Ce que tu dis :**
> "En quelques secondes, le commercial a un devis complet avec les prix du catalogue BTP. Sans l'IA, il aurait saisi tout ça à la main."

---

## Scénario 2 — L'email vague (3 min)

**Tu ouvres le devis de Karim Bensalem et tu dis :**
> "Maintenant un cas plus réaliste — les clients écrivent rarement des emails précis."

**Tu montres les alertes dans le panneau de droite :**
- *"Quantité imprécise — 'quelques' interprétée comme 3 — à confirmer avec le client"*
- *"Quantité imprécise — 'une dizaine' interprétée comme 10 — à confirmer avec le client"*
- L'alerte budget dépassé

**Tu joues le rôle du commercial qui corrige :**
- Tu changes la quantité "3" → "20" pour les sacs de ciment
- Tu cliques "Sauvegarder"

**Tu vas sur le Dashboard et tu montres la carte Feedback loop :**
- Corrections enregistrées augmente
- Précision IA se recalcule

**Ce que tu dis :**
> "L'IA ne bloque pas — elle extrait ce qu'elle peut et signale ce qu'elle n'est pas sûre. Chaque correction du commercial est enregistrée pour mesurer la précision de l'IA dans le temps. C'est ce qu'on appelle un feedback loop."

---

## Scénario 3 — L'email automatique (1 min)

**Tu ouvres le devis de Sophie Arnaud et tu dis :**
> "Ce troisième devis — je n'ai rien fait. Il est arrivé tout seul parce que l'application surveille la boîte mail toutes les 5 minutes. Le commercial arrive le matin et ses devis sont déjà générés."

---

## Dashboard — La cerise sur le gâteau (1 min)

**Tu montres :**
- Les KPI : total devis, montant moyen, taux de validation
- Les graphiques par statut
- La carte Feedback loop avec le taux de précision

**Ce que tu dis :**
> "Le manager voit l'activité en temps réel. Ce taux de précision de 88% — c'est une métrique qu'on ne peut calculer que parce que le système apprend des corrections humaines."

---

## Réponses aux questions du jury

**"Comment l'IA sait ce qu'est du ciment ?"**
> "Je lui fournis le catalogue BTP dans le prompt. Elle catégorise chaque article et applique la grille tarifaire : économique, standard ou premium."

**"Et si l'email est mal écrit ou en anglais ?"**
> "GPT-4o-mini comprend les emails mal écrits, avec des fautes, en plusieurs langues. C'est un avantage par rapport à une extraction par mots-clés classique."

**"C'est quoi le feedback loop concrètement ?"**
> "Quand le commercial change une quantité ou un prix, le système enregistre l'écart entre ce que l'IA avait proposé et ce que le commercial a mis. Ça me permet d'afficher un taux de précision et d'identifier les types d'erreurs récurrentes."

**"Pourquoi H2 et pas PostgreSQL ?"**
> "H2 en local pour simplifier le développement — pas besoin d'installer une base. En production sur Railway, l'application tourne avec PostgreSQL. La migration s'est faite en changeant uniquement la config Spring Boot, sans toucher une seule ligne de code métier — c'est l'avantage de Spring Data JPA."

**"Tu as combien de tests ?"**
> "64 tests backend automatisés — 39 unitaires sur les services avec Mockito, 10 d'intégration JPA avec @DataJpaTest, 15 controllers avec @WebMvcTest et MockMvc. Plus des tests frontend avec Vitest."

---

## Ordre recommandé sur les slides

1. Le problème (les commerciaux perdent du temps à saisir des devis manuellement)
2. La solution (pipeline IA : email → devis en quelques secondes)
3. Démo live (scénarios 1, 2, 3 + dashboard)
4. Stack technique (1 slide)
5. Ce que j'ai appris / perspectives d'évolution

---

*Entraîne-toi 2-3 fois avant la soutenance pour que la démo soit fluide.*
