# Agent Log

Met à jour HISTORIQUE.txt avec les changements de la session en cours.

## Étapes

1. Lire `/opt/gilmotech-brain/HISTORIQUE.txt` pour voir le numéro de la dernière session.

2. Faire un `git diff` et `git status` pour lister tous les changements :
```bash
cd /opt/gilmotech-brain && git diff --stat && git status
```

3. Ajouter une nouvelle entrée dans `HISTORIQUE.txt` avec ce format exact :

```
--------------------------------------------------------------------------------
SESSION N — YYYY-MM-DD : TITRE COURT
--------------------------------------------------------------------------------

CONTEXTE
  Pourquoi ce changement a été fait.

DÉCISIONS TECHNIQUES
  - Choix A plutôt que B, et pourquoi.

FICHIERS MODIFIÉS / CRÉÉS
  chemin/fichier.java
    → Ce qui a changé et pourquoi.

RÉSULTATS
  → Tests : X/Y OK
  → Build : OK / KO

CE QUI RESTE À FAIRE
  - Point 1
  - Point 2
```

4. Confirmer que l'entrée a bien été ajoutée en affichant les 20 dernières lignes :
```bash
tail -20 /opt/gilmotech-brain/HISTORIQUE.txt
```
