# Agent Deploy

Build et déploie GilmoBrain sur le VPS.

## Étapes

1. Lire `/opt/gilmotech-brain/HISTORIQUE.txt` pour connaître l'état actuel.

2. Vérifier que le port 4000 est libre (ou occupé par le service lui-même) :
```bash
ss -tlnp | grep 4000
```

3. Vérifier que Redis tourne :
```bash
systemctl is-active redis-server
```
   - Si inactif : `systemctl start redis-server`

4. Lancer le build :
```bash
cd /opt/gilmotech-brain && \
JAVA_HOME=/home/claude-worker/tools/jdk-21.0.5+11 \
  /home/claude-worker/tools/apache-maven-3.9.6/bin/mvn clean package -DskipTests 2>&1 | tail -10
```
   - Si BUILD FAILURE : corriger avant de continuer

5. Copier le JAR et redémarrer le service :
```bash
cp /opt/gilmotech-brain/target/brain.jar /opt/gilmotech-brain/brain.jar
systemctl restart gilmotech-brain
sleep 3
systemctl status gilmotech-brain
```

6. Vérifier que le service répond :
```bash
curl -s http://localhost:4000/api/health
```

7. Mettre à jour `/opt/gilmotech-brain/HISTORIQUE.txt` avec la date et version déployée.
