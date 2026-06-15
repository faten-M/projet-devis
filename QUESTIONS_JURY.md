# Questions du jury — DevisAI BTP
# Toutes les questions possibles avec les réponses exactes à dire
# Apprendre par coeur avant le 7 juillet 2026

# ⚠️ CHIFFRES EXACTS À RETENIR (tirés de ton rapport)
# - 64 tests backend (rapport du 6 juin) + tests feedback loop ajoutés depuis
# - 6 mois de stage (12 janvier → 19 juin 2026)
# - 2 à 6 heures de travail commercial économisées par jour
# - 20 à 40 emails de devis reçus par jour chez le client BTP
# - 2 à 4 secondes par appel OpenAI
# - Score de confiance entre 0 et 100%
# - Entreprise : Hudhudis, fondée le 13 novembre 2023 par Khalil Guedidi
# - Méthode : BMAD (Breakthrough Method of Agile AI-driven Development)

---

# PARTIE 1 — LE DISCOURS D'INTRODUCTION (à apprendre par coeur)

> "Bonjour. Mon projet de stage s'appelle DevisAI BTP.
>
> Le problème que j'ai résolu : dans les entreprises BTP, les commerciaux
> reçoivent des dizaines d'emails de clients qui demandent des devis.
> Ils doivent lire chaque email, identifier les produits, les quantités,
> le budget, puis saisir tout ça manuellement dans un logiciel. C'est
> long, répétitif, et source d'erreurs.
>
> Ma solution : une application qui lit automatiquement ces emails et
> génère des brouillons de devis structurés en quelques secondes grâce
> à l'intelligence artificielle.
>
> Le commercial n'a plus qu'à vérifier et valider. Le gain de temps est
> immédiat.
>
> Je vais vous faire une démonstration live."

---

# PARTIE 2 — QUESTIONS SUR LE PROJET (générales)

---

### Q : "Explique-moi ton projet en 30 secondes."

> "J'ai créé une application web pour les entreprises BTP. Elle lit
> automatiquement les emails des clients, extrait les informations grâce
> à GPT-4o-mini — les produits, les quantités, le budget, la date —
> et génère un brouillon de devis que le commercial n'a plus qu'à valider.
> Il y a aussi un tableau de bord avec des statistiques et un système
> de feedback loop qui mesure la précision de l'IA."

---

### Q : "Quel était ton cahier des charges ?"

> "L'objectif était de réduire le temps de saisie manuelle des devis BTP.
> Les fonctionnalités demandées étaient :
> la lecture automatique des emails via IMAP,
> l'extraction IA des informations,
> un éditeur de devis modifiable,
> l'export PDF,
> un tableau de bord de suivi,
> et un système pour mesurer la qualité de l'IA."

---

### Q : "Quelle est la partie dont tu es le plus fier ?"

> "Le pipeline IA complet — le fait que l'application parte d'un email
> brut et arrive à un devis structuré sans intervention humaine.
> Et aussi le feedback loop, parce que c'est une fonctionnalité originale
> qui mesure et améliore la qualité de l'IA dans le temps."

---

### Q : "C'était quoi la partie la plus difficile ?"

> "La partie la plus difficile a été le prompt engineering pour l'extraction IA.
> Au début, l'IA hallusinait des quantités ou inventait des produits qui
> n'étaient pas dans l'email. J'ai dû affiner le prompt plusieurs fois,
> ajouter des exemples, contraindre le format de sortie avec les
> Structured Outputs d'OpenAI pour avoir des résultats fiables."

---

### Q : "Tu as travaillé seule ?"

> "Oui, j'ai développé ce projet seule pendant mon stage.
> J'ai conçu l'architecture, codé le backend et le frontend,
> écrit les 77 tests, et rédigé la documentation."

---

### Q : "Combien de temps tu as mis ?"

> "Le projet a été développé sur la durée du stage, en plusieurs phases :
> d'abord l'architecture et le pipeline de base, puis l'interface React,
> ensuite les fonctionnalités avancées comme le feedback loop et les tests,
> et enfin la préparation au déploiement."

---

# PARTIE 2B — QUESTIONS SUR LA MÉTHODE ET L'ENTREPRISE

---

### Q : "C'est quoi la méthode BMAD ?"

> "BMAD signifie Breakthrough Method of Agile AI-driven Development.
> C'est une méthode de travail avec l'IA que mon tuteur m'a enseignée
> dès le début du stage. Elle repose sur 4 principes :
>
> 1. Comprendre l'architecture avant d'écrire du code
> 2. Toujours donner le contexte du projet à l'IA avant de lui demander
>    quelque chose — elle ne répond pas dans le vide
> 3. Tester et vérifier chaque réponse de l'IA avant de l'utiliser
> 4. Ne jamais copier du code sans comprendre ce qu'il fait
>
> Cette méthode m'a permis de garder le contrôle de ce que je produisais,
> au lieu de me laisser porter par l'IA sans comprendre le code."

---

### Q : "Pourquoi avoir commencé par des regex avant d'utiliser l'IA ?"

> "C'était une décision de conception progressive. On a d'abord développé
> une version fonctionnelle en Java pur avec des expressions régulières —
> sans dépendance externe, rapide, gratuite.
>
> Après avoir constaté les limites — les regex échouaient sur les quantités
> floues comme 'une dizaine', les budgets en texte libre, les dates
> en langage naturel — c'est là que mon tuteur m'a proposé d'intégrer
> GPT-4o-mini.
>
> L'avantage de cette approche : j'ai d'abord bien compris le problème
> avant d'utiliser l'IA. Je sais exactement ce que l'IA fait en plus
> par rapport aux règles classiques."

---

### Q : "C'est quoi Tella ?"

> "Tella est un outil d'enregistrement de vidéos courtes asynchrones.
> Mon tuteur l'utilisait pour m'expliquer des points techniques sans
> avoir à caler une réunion en direct. Je pouvais revoir l'explication
> autant de fois que nécessaire à mon rythme.
> C'est très pratique en full remote."

---

### Q : "Comment tu travaillais en full remote ?"

> "Les échanges quotidiens passaient par Slack pour les questions et
> le suivi. Jira pour les tickets et les livrables. Tella pour les
> explications techniques de mon tuteur en vidéo asynchrone.
> GitHub avec des Pull Requests que mon tuteur relisait et validait
> avant chaque fusion.
>
> Ce mode de travail m'a appris à m'organiser seule, à communiquer
> clairement par écrit, et à justifier mes choix techniques à l'écrit
> dans les Pull Requests."

---

# PARTIE 3 — QUESTIONS TECHNIQUES

---

### Q : "C'est quoi GPT-4o-mini ? Pourquoi ce modèle ?"

> "GPT-4o-mini c'est un modèle de langage d'OpenAI. Il comprend le texte,
> extrait des informations, classe des données.
>
> Avec mon tuteur, nous avons comparé trois options :
> un modèle open source local comme Llama, GPT-4o classique, et GPT-4o-mini.
> Le modèle local évitait les coûts mais demandait un serveur GPU
> et donnait une qualité moindre en français technique.
> GPT-4o classique était le plus performant mais 5 à 10 fois plus cher.
>
> Nous avons retenu GPT-4o-mini car il donnait de bons résultats sur
> les emails testés, avec un coût très faible — quelques centimes pour
> plusieurs centaines d'emails — et un temps de réponse inférieur à
> 2 secondes par appel."

---

### Q : "C'est quoi les Structured Outputs ?"

> "C'est une fonctionnalité d'OpenAI qui garantit que la réponse de l'IA
> respecte exactement un schéma JSON que j'ai défini.
>
> Sans ça, l'IA pourrait répondre en texte libre, oublier un champ,
> ou écrire un JSON mal formé. Mon code Java crasherait en essayant
> de le lire.
>
> Avec les Structured Outputs, je lui dis 'tu dois répondre avec
> ces champs précis, ces types précis' — et elle le respecte toujours.
> Zéro exception, zéro parsing manuel."

---

### Q : "C'est quoi le prompt engineering ?"

> "C'est l'art d'écrire les instructions qu'on donne à l'IA pour qu'elle
> fasse ce qu'on veut. L'IA ne comprend pas le code — elle comprend
> le texte en français.
>
> Mon prompt dit par exemple :
> 'Tu es un assistant spécialisé BTP. Extrait les produits, quantités
> et budget de cet email. Si une quantité est floue comme 'quelques',
> mets une valeur estimée et signale-la dans les alertes.'
>
> Le résultat de l'IA dépend directement de la qualité du prompt."

---

### Q : "C'est quoi IMAP ?"

> "IMAP c'est le protocole standard pour lire les emails depuis un
> serveur de messagerie. C'est ce qu'utilise Outlook, Thunderbird,
> Gmail sur téléphone — ils se connectent tous via IMAP.
>
> Mon application Java se connecte à Gmail via IMAP toutes les
> 5 minutes, lit les nouveaux emails non lus, les traite,
> et les marque comme lus. Le commercial peut aussi déclencher
> le scan manuellement en cliquant 'Scanner les emails'."

---

### Q : "C'est quoi Spring Boot ?"

> "Spring Boot c'est un framework Java pour créer des applications web
> et des API REST. Il gère automatiquement beaucoup de choses :
> la connexion à la base de données, les routes HTTP, la validation,
> la sécurité de base.
>
> Sans Spring Boot je devrais configurer tout ça manuellement.
> Avec Spring Boot je me concentre sur le code métier."

---

### Q : "C'est quoi une API REST ?"

> "C'est une interface de communication entre le frontend React
> et le backend Spring Boot.
>
> Quand React veut afficher la liste des devis, il envoie une
> requête HTTP GET à /api/devis.
> Spring Boot reçoit la requête, interroge la base de données,
> et répond avec un JSON.
>
> C'est comme un serveur de restaurant : le client (React) passe
> une commande, le serveur (API) va chercher en cuisine (base de données)
> et ramène le plat (JSON)."

---

### Q : "C'est quoi JPA et Hibernate ?"

> "JPA c'est un standard Java pour sauvegarder des objets Java
> dans une base de données. Hibernate c'est l'implémentation de JPA.
>
> Sans JPA, j'écrirais des requêtes SQL manuellement :
> INSERT INTO devis VALUES (...).
>
> Avec JPA, j'écris juste quoteRepository.save(monDevis) et
> Hibernate génère le SQL tout seul. C'est beaucoup plus rapide
> à développer et moins d'erreurs."

---

### Q : "C'est quoi le feedback loop exactement ?"

> "Le feedback loop c'est un système d'apprentissage.
>
> Quand l'IA génère un devis, elle propose des valeurs :
> quantité 20, prix 8,50€.
>
> Si le commercial corrige la quantité de 20 à 15, mon application
> enregistre cet écart dans une table CorrectionIA.
>
> En cumulant toutes ces corrections, je calcule un taux de précision :
> si sur 100 lignes de devis, le commercial en a corrigé 10,
> la précision est de 90%.
>
> Ce taux s'affiche sur le tableau de bord. Il permet de voir
> si l'IA s'améliore ou se dégrade dans le temps."

---

### Q : "Comment tu calcules le taux de précision ?"

> "La formule est :
> (1 - nombre de corrections / nombre de lignes validées) × 100
>
> Par exemple : 1 correction sur 9 lignes validées =
> (1 - 1/9) × 100 = 88,9%
>
> Je ne compte que les lignes des devis avec le statut PRÊT,
> parce que ce sont les seuls qui ont été validés par un commercial.
> Les brouillons non validés ne sont pas comptés."

---

### Q : "Tu as combien de tests et c'est quoi ?"

> "Mon rapport mentionne 64 tests automatisés backend au moment de la
> remise du livrable écrit le 6 juin. Depuis, j'ai ajouté des tests pour
> le feedback loop, notamment pour la table CorrectionIA et le calcul
> du taux de précision.
>
> Les 64 tests sont répartis en 3 niveaux :
>
> 1. 39 tests unitaires avec Mockito : je teste chaque service isolément
>    en simulant les dépendances. Par exemple je teste que le calcul
>    du devis brouillon est correct sans appeler OpenAI.
>
> 2. 10 tests d'intégration @DataJpaTest : je teste les repositories
>    avec une vraie base H2 en mémoire pour vérifier que les requêtes
>    SQL fonctionnent.
>
> 3. 15 tests de controllers avec @WebMvcTest et MockMvc : je simule
>    des appels HTTP et vérifie que les controllers répondent avec le
>    bon status et le bon JSON.
>
> En complément, j'ai aussi des tests frontend avec Vitest et Testing
> Library pour vérifier que les pages React affichent correctement
> les données."

---

### Q : "Comment tu sécurises la clé API OpenAI ?"

> "La clé API n'est jamais écrite dans le code.
> Elle est dans une variable d'environnement appelée OPENAI_API_KEY.
>
> Dans application.properties j'écris :
> openai.api.key=${OPENAI_API_KEY}
>
> En local je définis la variable dans mon terminal.
> En production sur Railway je la configure dans l'interface.
>
> Comme ça, si quelqu'un regarde le code sur GitHub,
> il ne voit aucun secret."

---

### Q : "C'est quoi Docker ?"

> "Docker c'est un outil qui emballe l'application et tout ce dont
> elle a besoin — Java, Node.js, les dépendances — dans une boîte
> appelée conteneur.
>
> Cette boîte est identique partout : sur mon PC, sur le serveur
> Railway, n'importe où. Ça évite le problème classique
> 'ça marche sur ma machine mais pas en production'.
>
> Dans mon rapport, la conteneurisation était prévue comme dernière
> étape de la phase 6. Je l'ai réalisée entre la remise du rapport
> le 6 juin et aujourd'hui : j'ai écrit un Dockerfile multi-étapes
> qui compile d'abord le frontend React, ensuite Spring Boot avec
> le frontend intégré, et crée une image légère pour la production."

---

### Q : "C'est quoi la différence entre H2 et PostgreSQL ?"

> "Les deux sont des bases de données relationnelles qui utilisent SQL.
> Le code Java est identique pour les deux.
>
> H2 est une base légère qui vit dans un fichier sur le PC.
> Elle est parfaite pour le développement car elle ne nécessite
> aucune installation.
>
> PostgreSQL est un vrai serveur de base de données, plus robuste,
> conçu pour la production.
>
> J'utilise H2 en local et PostgreSQL sur Railway en production.
> La migration s'est faite en changeant uniquement la configuration
> Spring Boot — pas une seule ligne de code métier n'a changé.
> C'est l'avantage de Spring Data JPA."

---

# PARTIE 4 — QUESTIONS PIÈGES (les plus difficiles)

---

### PIÈGE Q : "Et si l'email est en anglais ou dans une autre langue ?"

> "L'application est conçue pour traiter des emails en français, ce qui
> correspond au contexte BTP français ciblé. Je n'ai pas testé d'autres
> langues dans ce projet et mon rapport le mentionne clairement.
> GPT-4o-mini est techniquement capable de comprendre plusieurs langues,
> donc le support multilingue serait une évolution possible, mais ce
> n'est pas une fonctionnalité que j'ai développée ni testée."

---

### PIÈGE Q : "C'est vraiment de l'IA ou c'est juste du regex ?"

> "C'est une vraie distinction importante. Non, ce n'est pas du regex.
>
> Un regex cherche des patterns fixes comme '200 sacs'.
> Il échoue complètement sur 'j'aurais besoin d'une dizaine de parpaings
> environ' parce que la quantité n'est pas explicite.
>
> GPT-4o-mini comprend le sens. Il comprend que 'une dizaine' c'est
> environ 10, que 'quelques' c'est 3 à 5, que 'pas trop cher'
> indique une contrainte de budget sans chiffre précis.
>
> C'est ça la différence entre un parsing basé sur les règles
> et un modèle de langage."

---

### PIÈGE Q : "À quoi ça sert si le commercial doit vérifier de toute façon ?"

> "Le commercial ne fait plus la saisie — il fait juste la vérification.
> Ce sont deux choses très différentes.
>
> Saisir prend 10 à 15 minutes par email : lire, chercher les références
> produits, taper les quantités, calculer les totaux.
>
> Vérifier prend 1 à 2 minutes : regarder le devis pré-rempli,
> corriger si besoin, valider.
>
> Pour 20 emails par jour, c'est 3 à 4 heures de travail répétitif
> évitées. Le commercial peut se concentrer sur la relation client."

---

### PIÈGE Q : "Et si OpenAI tombe en panne ?"

> "C'est un risque réel que j'ai identifié. Actuellement l'application
> dépend d'OpenAI. Si l'API est indisponible, l'extraction échoue.
>
> Pour l'instant, l'email reste dans la boite de réception
> et peut être retraité plus tard.
>
> En évolution, on pourrait ajouter un mécanisme de retry automatique
> et une notification d'alerte si l'API ne répond pas."

---

### PIÈGE Q : "Les emails des clients sont confidentiels. Comment tu gères la RGPD ?"

> "C'est une question importante. Actuellement, les emails sont
> envoyés à l'API OpenAI pour traitement, ce qui signifie
> qu'ils quittent l'infrastructure de l'entreprise.
>
> Pour une mise en production réelle, il faudrait :
> soit un accord de traitement des données avec OpenAI,
> soit utiliser un modèle IA hébergé localement (comme Llama)
> pour que les données ne quittent jamais l'entreprise.
>
> C'est une limite que j'ai identifiée et qui ferait partie
> des évolutions prioritaires."

---

### PIÈGE Q : "Pourquoi tu ne gardes pas l'historique des modifications du devis ?"

> "La table CorrectionIA enregistre les changements de valeurs
> quand le commercial valide un devis. C'est le feedback loop.
>
> Un historique complet de toutes les modifications intermédiaires
> n'a pas été implémenté dans ce projet, mais c'est une évolution
> naturelle — on pourrait ajouter une table ModificationHistory
> avec un enregistrement à chaque sauvegarde."

---

### PIÈGE Q : "Pourquoi scanner toutes les 5 minutes et pas en temps réel ?"

> "Le scan par polling toutes les 5 minutes est une approche
> simple et fiable. Pour du temps réel, il faudrait utiliser
> les webhooks Gmail ou Gmail Push Notifications via Google Pub/Sub —
> c'est techniquement possible mais beaucoup plus complexe à mettre
> en place et à maintenir.
>
> Pour l'usage BTP où les devis ne sont pas urgents à la seconde,
> 5 minutes est un délai acceptable et le compromis simplicité/
> fonctionnalité est bon."

---

### PIÈGE Q : "Ton taux de précision peut être biaisé non ?"

> "Oui, c'est une limite que j'ai réfléchie.
>
> Le taux mesure le nombre de corrections par rapport aux lignes
> validées. Mais si le commercial est peu rigoureux et ne corrige
> pas des valeurs incorrectes, le taux sera artificiellement élevé.
>
> À l'inverse, si le commercial est très minutieux, le taux sera bas
> même si l'IA était presque juste.
>
> C'est une métrique indicative. Elle donne une tendance,
> pas une mesure absolue de qualité."

---

### PIÈGE Q : "Pourquoi pas utiliser une IA open source locale ?"

> "C'est une alternative valide, notamment pour la confidentialité.
> Les modèles comme Llama 3 ou Mistral peuvent tourner localement.
>
> J'ai choisi GPT-4o-mini pour deux raisons :
> la qualité d'extraction est supérieure sur des textes ambigus,
> et c'est beaucoup plus simple à intégrer via l'API OpenAI.
>
> En production avec des données confidentielles, passer à un modèle
> local serait la bonne décision."

---

### PIÈGE Q : "Tes tests sont-ils vraiment des tests d'intégration ?"

> "Les tests @DataJpaTest sont des tests d'intégration au sens Spring —
> ils utilisent une vraie base H2 en mémoire et testent vraiment
> les requêtes SQL générées par Hibernate.
>
> Les tests @WebMvcTest sont des tests de couche web — ils testent
> le comportement des controllers HTTP avec des dépendances simulées.
>
> Les vrais tests d'intégration end-to-end (qui testeraient toute
> la chaîne de email à PDF) n'ont pas été implémentés car ils
> nécessiteraient une connexion Gmail et une clé OpenAI en test,
> ce qui sort du cadre des tests automatisés classiques."

---

### PIÈGE Q : "Comment tu gères les emails avec des pièces jointes ?"

> "Actuellement l'application traite uniquement le corps texte de l'email.
> Les pièces jointes comme des PDFs ou des tableaux Excel sont ignorées.
>
> C'est une limite connue. Pour les traiter, il faudrait ajouter
> l'extraction de texte depuis PDF (Apache PDFBox) et depuis Excel
> (Apache POI), puis les concaténer au corps de l'email avant
> l'envoi à l'IA."

---

### PIÈGE Q : "Pourquoi Spring Boot et pas Node.js ou Python pour l'IA ?"

> "Python est souvent associé à l'IA mais ici je n'entraîne pas
> de modèle — j'appelle une API OpenAI. Cette API est disponible
> dans tous les langages.
>
> J'ai choisi Spring Boot parce que c'est le framework que j'ai
> étudié, et parce que Spring Data JPA, la validation, le scheduling
> des emails — tout ça est très bien intégré dans l'écosystème Spring.
> Il n'y avait pas d'avantage technique à utiliser Python ici."

---

# PARTIE 5 — QUESTIONS SUR LE BILAN ET LES AMÉLIORATIONS

---

### Q : "Qu'est-ce que tu ferais différemment ?"

> "Si je recommençais, j'écrirais les tests en parallèle du développement
> plutôt qu'après. J'aurais aussi commencé par définir le schéma de base
> de données plus précisément pour éviter des refactorisations en cours
> de route.
>
> Pour le prompt IA, j'aurais fait plus de tests sur des cas limites
> dès le début — les emails très courts, les emails avec des fautes graves,
> les emails avec des demandes très vagues sans aucune quantité."

---

### Q : "Quelles évolutions tu ajouterais ?"

> "Trois évolutions prioritaires :
>
> 1. Modèle IA local (Llama ou Mistral) pour respecter la RGPD
>    sur les données clients confidentielles.
>
> 2. Apprentissage actif — utiliser les corrections du feedback loop
>    pour fine-tuner le modèle sur des données BTP spécifiques.
>
> 3. Application mobile — une version mobile simple pour que le
>    commercial valide les devis depuis son téléphone."

---

### Q : "Qu'est-ce que tu as appris pendant ce stage ?"

> "J'ai appris à construire un pipeline IA complet de bout en bout —
> ce que je n'avais jamais fait avant.
>
> Techniquement : l'API OpenAI et les Structured Outputs,
> Spring Data JPA avec les relations entre entités,
> le testing avec Mockito et MockMvc,
> React avec TypeScript et la gestion d'état,
> Docker et le déploiement en production.
>
> Méthodologiquement : j'ai appris à découper un projet complexe
> en phases, à écrire des tests qui me donnent confiance dans mon code,
> et à documenter pour que quelqu'un d'autre puisse reprendre le projet."

---

# PARTIE 6 — CONSEILS POUR LA PRÉSENTATION

---

## Comment parler pendant la démo

- Dis ce qui va se passer AVANT de cliquer
  → "Je vais cliquer sur Scanner les emails... et vous voyez, 3 devis
     apparaissent en quelques secondes"

- Explique pendant que ça charge
  → "Là l'IA traite l'email, ça prend 2 à 3 secondes..."

- Montre les détails importants
  → "Regardez ici — le nom Thomas Mercier a été extrait automatiquement
     depuis la signature de l'email"

---

## Si tu ne sais pas répondre à une question

Ne dis jamais "je ne sais pas" tout seul.

Dis :
> "C'est une bonne question. Ce point n'a pas été implémenté dans
> ce projet mais la solution serait de..."

OU

> "Je n'ai pas approfondi ce point dans le cadre de ce stage,
> mais j'ai identifié cette limite dans les évolutions possibles."

---

## Si la démo plante

Reste calme. Dis :
> "Il y a un problème technique, je vais relancer rapidement."
> Relance le backend avec mvn spring-boot:run
> Relance le frontend avec npm run dev

Si ça ne marche toujours pas, montre les screenshots ou explique
oralement — le jury évalue ta compréhension, pas juste la démo.

---

## Le ton à adopter

- Parle lentement et distinctement
- Regarde le jury, pas l'écran
- Sois fière — tu as construit quelque chose de complet et fonctionnel
- Les questions difficiles sont un signe d'intérêt, pas d'hostilité

---

## Les 5 phrases clés à retenir absolument

1. "L'IA ne remplace pas le commercial — elle lui fait gagner du temps
   sur la saisie pour qu'il se concentre sur la relation client."

2. "Les Structured Outputs garantissent un JSON valide à chaque appel —
   c'est ce qui rend le pipeline fiable en production."

3. "Le feedback loop mesure la précision de l'IA dans le temps —
   c'est une métrique qu'on ne peut calculer que parce que le système
   apprend des corrections humaines."

4. "Tous les secrets sont dans des variables d'environnement —
   aucun mot de passe n'est dans le code ni sur GitHub."

5. "La migration H2 vers PostgreSQL n'a nécessité aucun changement
   de code métier — Spring Data JPA abstrait la base de données."

---

*Bonne chance pour le 7 juillet. Tu connais ce projet mieux que personne.*
