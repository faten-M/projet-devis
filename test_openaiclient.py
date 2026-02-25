#!/usr/bin/env python3
"""
Script de test pour OpenAiClient.java

Ce script effectue une analyse statique du fichier OpenAiClient.java
et valide sa structure et ses méthodes principales.
"""

import re
import sys
from pathlib import Path

class OpenAiClientAnalyzer:
    """Analyseur pour valider la structure de OpenAiClient.java"""
    
    def __init__(self, file_path):
        self.file_path = Path(file_path)
        self.content = self.file_path.read_text(encoding='utf-8')
        self.errors = []
        self.warnings = []
        self.tests_passed = 0
        self.tests_total = 0
    
    def test(self, name, condition):
        """Effectue un test et enregistre le résultat"""
        self.tests_total += 1
        if condition:
            self.tests_passed += 1
            print(f"✓ {name}")
            return True
        else:
            self.errors.append(f"✗ {name}")
            print(f"✗ {name}")
            return False
    
    def warning(self, name, msg):
        """Enregistre un avertissement"""
        self.warnings.append(f"⚠ {name}: {msg}")
        print(f"⚠ {name}: {msg}")
    
    def analyze(self):
        """Effectue l'analyse complète du fichier"""
        print("=" * 60)
        print("ANALYSE DU FICHIER OpenAiClient.java")
        print("=" * 60)
        
        # Test 1: Vérifier que c'est un fichier Java valide
        self.test("Le fichier a l'extension .java", 
                  self.file_path.suffix == ".java")
        
        # Test 2: Vérifier la déclaration du package
        self.test("Le package est défini correctement",
                  "package com.projetdevis.service;" in self.content)
        
        # Test 3: Vérifier la déclaration de la classe
        self.test("La classe OpenAiClient est définie",
                  "class OpenAiClient" in self.content)
        
        # Test 4: Vérifier la déclaration du constructeur
        self.test("Le constructeur public est défini",
                  "public OpenAiClient()" in self.content)
        
        # Test 5: Vérifier la présence de la méthode retirerSignature
        self.test("La méthode retirerSignature(String) existe",
                  "public String retirerSignature(String email)" in self.content)
        
        # Test 6: Vérifier la présence de la méthode supprimerRepetitions
        self.test("La méthode supprimerRepetitions(String) existe",
                  "public String supprimerRepetitions(String texte)" in self.content)
        
        # Test 7: Vérifier la gestion de null dans retirerSignature
        self.test("retirerSignature gère les entrées null/vides",
                  'if (email == null || email.isBlank())' in self.content)
        
        # Test 8: Vérifier la gestion de null dans supprimerRepetitions
        self.test("supprimerRepetitions gère les entrées null/vides",
                  'if (texte == null || texte.isBlank())' in self.content)
        
        # Test 9: Vérifier la vérification de la clé API
        self.test("Le constructeur vérifie la clé API OpenAI",
                  'System.getenv("OPENAI_API_KEY")' in self.content)
        
        # Test 10: Vérifier la levée d'exception pour clé API manquante
        self.test("Une exception est levée si OPENAI_API_KEY est manquante",
                  'throw new IllegalStateException' in self.content)
        
        # Test 11: Vérifier la méthode privée callOpenAiApi
        self.test("La méthode privée callOpenAiApi existe",
                  "private String callOpenAiApi(String prompt, String operationName)" in self.content)
        
        # Test 12: Vérifier les imports OpenAI
        self.test("Les imports OpenAI SDK sont présents",
                  'import com.openai.models.ChatCompletion;' in self.content)
        
        # Test 13: Vérifier la gestion des erreurs
        self.test("La gestion des erreurs est implémentée",
                  "try {" in self.content and "catch (Exception e)" in self.content)
        
        # Test 14: Vérifier la javadoc pour le constructeur
        self.test("Le constructeur a une documentation Javadoc",
                  "/**" in self.content and "Constructeur :" in self.content)
        
        # Test 15: Vérifier que le modèle est défini
        self.test("Le modèle GPT est défini",
                  'MODEL = "gpt-' in self.content)
        
        # Avertissements
        if 'gpt-5-nano' in self.content:
            self.warning("Modèle GPT-5", "Le modèle gpt-5-nano peut ne pas être disponible dans l'API réelle")
        
        if 'System.err.println(errorMsg)' in self.content:
            self.warning("Logs d'erreur", "Les erreurs sont loggées sur System.err")
        
        return self.get_results()
    
    def get_results(self):
        """Retourne un résumé des résultats"""
        print("\n" + "=" * 60)
        print(f"RÉSULTATS: {self.tests_passed}/{self.tests_total} tests réussis")
        print("=" * 60)
        
        if self.warnings:
            print("\nAvertissements:")
            for warning in self.warnings:
                print(f"  {warning}")
        
        if self.errors:
            print("\nErreurs trouvées:")
            for error in self.errors:
                print(f"  {error}")
            return False
        
        print("\n✓ Tous les tests sont passés!")
        return True

def main():
    """Fonction principale"""
    file_path = Path("src/main/java/com/projetdevis/service/OpenAiClient.java")
    
    if not file_path.exists():
        print(f"Erreur: Le fichier {file_path} n'existe pas")
        sys.exit(1)
    
    analyzer = OpenAiClientAnalyzer(file_path)
    success = analyzer.analyze()
    
    sys.exit(0 if success else 1)

if __name__ == "__main__":
    main()
