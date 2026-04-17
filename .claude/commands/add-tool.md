# Agent Add Tool

Ajoute un nouveau tool à GilmoBrain.

## Étapes

1. Lire `/opt/gilmotech-brain/HISTORIQUE.txt` et la liste des tools existants dans
   `src/main/java/be/gilmotech/brain/tools/ToolsRegistry.java`.

2. Demander si ce n'est pas déjà précisé :
   - Nom du tool (snake_case, ex: `calendar_event`)
   - Ce qu'il fait
   - API externe utilisée (si applicable)
   - Paramètres attendus

3. Créer le fichier `NomTool.java` dans `src/main/java/be/gilmotech/brain/tools/` :
   - Annoter avec `@Component`, `@Slf4j`, `@RequiredArgsConstructor`
   - Logger chaque appel : nom + durée + résultat tronqué à 100 chars
   - Gérer les erreurs proprement (throw RuntimeException avec message clair)
   - Utiliser des records pour les résultats

4. Enregistrer le tool dans `ToolsRegistry.java` :
   - Injecter le nouveau tool dans le constructeur
   - Ajouter le `GroqTool` dans `getGroqTools()` avec description claire et schéma JSON
   - Ajouter le `case` dans `executeTool()`

5. Ajouter la clé API dans `.env` et `application.properties` si nécessaire.

6. Écrire un test unitaire `NomToolTest.java` dans
   `src/test/java/be/gilmotech/brain/tools/`.

7. Lancer les tests :
```bash
cd /opt/gilmotech-brain && \
JAVA_HOME=/home/claude-worker/tools/jdk-21.0.5+11 \
  /home/claude-worker/tools/apache-maven-3.9.6/bin/mvn test \
  -Dtest="NomToolTest" 2>&1 | tail -20
```

8. Mettre à jour `/opt/gilmotech-brain/HISTORIQUE.txt` avec le nouveau tool ajouté.
