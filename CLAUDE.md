# GilmoBrain — Instructions pour Claude Code

## Règle n°1 — Lire l'historique en premier

Au début de chaque session, lire obligatoirement :
- `/opt/gilmotech-brain/HISTORIQUE.txt` → pour savoir où en est le projet
- `/opt/gilmotech-brain/DEPLOIEMENT.txt` → si la tâche touche au déploiement

Ne jamais supposer l'état du projet sans avoir lu ces fichiers.

## Règle n°2 — Mettre à jour l'historique à la fin de chaque changement

Après chaque modification (nouvelle fonctionnalité, bugfix, refactor, config),
ajouter une entrée dans `/opt/gilmotech-brain/HISTORIQUE.txt` avec ce format :

```
--------------------------------------------------------------------------------
SESSION N — YYYY-MM-DD : TITRE COURT DU CHANGEMENT
--------------------------------------------------------------------------------

CONTEXTE
  Pourquoi ce changement a été fait.

DÉCISIONS TECHNIQUES
  - Choix A plutôt que B, et pourquoi.

FICHIERS MODIFIÉS / CRÉÉS
  chemin/fichier.java
    → Ce qui a changé et pourquoi.

RÉSULTATS
  → Tests OK / KO
  → Build OK / KO

CE QUI RESTE À FAIRE
  - Point 1
  - Point 2
```

## Environnement

- Java 21 : `/home/claude-worker/tools/jdk-21.0.5+11/bin/java`
- Maven : `/home/claude-worker/tools/apache-maven-3.9.6/bin/mvn`
- Port : **4000**
- Sandbox bash/file : `/opt/brain-sandbox`
- Redis : `localhost:6379`

## Commandes de build et test

```bash
# Build
JAVA_HOME=/home/claude-worker/tools/jdk-21.0.5+11 \
  /home/claude-worker/tools/apache-maven-3.9.6/bin/mvn clean package -DskipTests

# Tests unitaires (sans Redis)
JAVA_HOME=/home/claude-worker/tools/jdk-21.0.5+11 \
  /home/claude-worker/tools/apache-maven-3.9.6/bin/mvn test \
  -Dtest="BashToolTest,FileToolTest"

# Tous les tests (Redis requis)
JAVA_HOME=/home/claude-worker/tools/jdk-21.0.5+11 \
  /home/claude-worker/tools/apache-maven-3.9.6/bin/mvn test
```

## Contraintes à respecter

- Clés API uniquement via `.env`, jamais dans le code
- Tout accès fichier/bash strictement limité à `/opt/brain-sandbox`
- CORS configuré pour `familychat.gilmotech.be` et `localhost:3000`
- Logs sur chaque appel tool : nom + durée + résultat tronqué à 100 chars
- Java 21 : utiliser records, pattern matching, sealed classes si pertinent
- Pas de commentaires inutiles dans le code
- Ne jamais committer `.env`

## Stack

Spring Boot 3.2.5 / Java 21 / Groq API (llama-3.3-70b-versatile) / Redis / Maven
