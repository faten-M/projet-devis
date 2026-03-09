
# Projet de stage – Automatisation de création de devis

##  Objectif du projet
Ce projet a pour but de générer automatiquement un devis brouillon à partir d’un e-mail client non structuré.  
Le système doit extraire les informations pertinentes, identifier les données manquantes ou ambiguës, puis produire un devis pré-rempli validable par un humain.

##  Périmètre du MVP
### Entrée
- Texte brut d’un e-mail client (copié/collé)
- Pas de pièces jointes dans la première version
- Ignorer signatures, disclaimers et réponses précédentes

### Sorties
- Un devis brouillon structuré (format JSON)
- Liste des champs :
  - identifiés
  - manquants
  - ambigus / incertains
- Un score de confiance global (0–100)

## Technologies utilisées
- Java 17  
- Maven  
- VS Code  

## État actuel du projet
- Migration depuis Node.js vers Java/Maven  
- Structure Maven fonctionnelle  
- Base prête pour implémenter la logique métier (extraction, analyse, génération de devis)

## Prochaines étapes
- Implémentation de l’analyse du texte  
- Détection des champs manquants / ambigus  
- Génération du JSON final  
- Ajout de tests unitaires  




