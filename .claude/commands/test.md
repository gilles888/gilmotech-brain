# Agent Test

Lance les tests et rapporte les résultats.

## Étapes

1. Lire `/opt/gilmotech-brain/HISTORIQUE.txt` pour connaître les tests existants.

2. Lancer les tests unitaires (sans Redis) :
```bash
cd /opt/gilmotech-brain && \
JAVA_HOME=/home/claude-worker/tools/jdk-21.0.5+11 \
  /home/claude-worker/tools/apache-maven-3.9.6/bin/mvn test \
  -Dtest="BashToolTest,FileToolTest,AgentControllerTest" 2>&1 | tail -30
```

3. Vérifier si Redis est actif pour les tests d'intégration :
```bash
redis-cli ping 2>/dev/null
```
   - Si Redis actif : lancer aussi `mvn test` complet
   - Si Redis inactif : préciser que BrainApplicationTests est ignoré

4. Pour chaque test en échec :
   - Afficher le message d'erreur complet
   - Identifier la cause
   - Proposer et appliquer le correctif
   - Relancer jusqu'à ce que tous les tests passent

5. Résumer : X/Y tests OK, liste des tests ignorés et pourquoi.

6. Mettre à jour `/opt/gilmotech-brain/HISTORIQUE.txt` avec les résultats.
