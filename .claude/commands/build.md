# Agent Build

Lance le build Maven et rapporte le résultat.

## Étapes

1. Lire `/opt/gilmotech-brain/HISTORIQUE.txt` pour connaître l'état actuel.

2. Lancer le build :
```bash
cd /opt/gilmotech-brain && \
JAVA_HOME=/home/claude-worker/tools/jdk-21.0.5+11 \
  /home/claude-worker/tools/apache-maven-3.9.6/bin/mvn clean package -DskipTests 2>&1 | tail -20
```

3. Si BUILD SUCCESS :
   - Indiquer la taille du JAR (`ls -lh target/brain.jar`)
   - Confirmer que `target/brain.jar` est prêt

4. Si BUILD FAILURE :
   - Afficher les erreurs de compilation
   - Identifier les fichiers concernés
   - Proposer et appliquer les corrections
   - Relancer le build jusqu'à succès

5. Mettre à jour `/opt/gilmotech-brain/HISTORIQUE.txt` si des fichiers ont été corrigés.
