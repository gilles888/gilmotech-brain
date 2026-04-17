# Agent Check

Vérifie l'état complet du projet et du service sur le VPS.

## Étapes

1. Lire `/opt/gilmotech-brain/HISTORIQUE.txt` pour connaître la dernière version déployée.

2. Vérifier le service systemd :
```bash
systemctl status gilmotech-brain
```

3. Vérifier Redis :
```bash
systemctl is-active redis-server && redis-cli ping
```

4. Vérifier que le port 4000 répond :
```bash
curl -s http://localhost:4000/api/health | python3 -m json.tool 2>/dev/null || \
curl -s http://localhost:4000/api/health
```

5. Vérifier les logs récents (erreurs éventuelles) :
```bash
journalctl -u gilmotech-brain --since "1 hour ago" | grep -E "ERROR|WARN|Exception" | tail -20
```

6. Vérifier l'espace disque et mémoire :
```bash
df -h /opt && free -h
```

7. Résumer l'état : service UP/DOWN, Redis OK/KO, dernière erreur si présente, 
   espace disque restant.
