# GilmoBrain

API Spring Boot centrale qui orchestre les interactions IA du VPS vmi2936009.

## Port retenu : 4000

### Ports occupés au moment du déploiement
| Port | Service |
|------|---------|
| 80 / 443 | Nginx |
| 8000 | Nginx (proxy) |
| 8081 | HygieneCheck (Java) |
| 8082 | Java (autre app) |
| 5432 | PostgreSQL |
| 22 | SSH |

Port 4000 retenu (premier libre parmi 4000, 4001, 4002, 4100).

## Clés API nécessaires

| Variable | Service | Où obtenir |
|----------|---------|------------|
| `GROQ_API_KEY` | Groq LLM | https://console.groq.com/keys |
| `BRAVE_API_KEY` | Brave Search | https://api.search.brave.com/app/keys |
| `OPENWEATHER_API_KEY` | OpenWeatherMap | https://home.openweathermap.org/api_keys |

## Installation

### 1. Installer Redis
```bash
apt install redis-server -y
systemctl enable redis-server
systemctl start redis-server
```

### 2. Créer le sandbox
```bash
mkdir -p /opt/brain-sandbox
chmod 755 /opt/brain-sandbox
```

### 3. Configurer les clés API
```bash
nano /opt/gilmotech-brain/.env
# Remplir GROQ_API_KEY, BRAVE_API_KEY, OPENWEATHER_API_KEY
```

### 4. Déployer le service systemd
```bash
cp /opt/gilmotech-brain/gilmotech-brain.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable gilmotech-brain
```

### 5. Déployer Nginx
```bash
cp /opt/gilmotech-brain/nginx-gilmotech-brain.conf /etc/nginx/sites-available/gilmotech-brain
ln -s /etc/nginx/sites-available/gilmotech-brain /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx
certbot --nginx -d brain.gilmotech.be
```

## Déploiement

```bash
cd /opt/gilmotech-brain
./deploy.sh
```

## Exemples curl

### Health check
```bash
curl https://brain.gilmotech.be/api/health \
  -H "X-Brain-Token: BRAIN_SECRET_TOKEN"
```

### Météo à Bruxelles
```bash
curl -X POST https://brain.gilmotech.be/api/agent/run \
  -H "Content-Type: application/json" \
  -H "X-Brain-Token: BRAIN_SECRET_TOKEN" \
  -d '{
    "task": "Quel temps fait-il à Bruxelles ?",
    "sessionId": "test-001",
    "mode": "conversation"
  }'
```

### Historique de session
```bash
curl https://brain.gilmotech.be/api/agent/memory/test-001 \
  -H "X-Brain-Token: BRAIN_SECRET_TOKEN"
```

### Supprimer une session
```bash
curl -X DELETE https://brain.gilmotech.be/api/agent/memory/test-001 \
  -H "X-Brain-Token: BRAIN_SECRET_TOKEN"
```

## Build local (sans déployer)
```bash
JAVA_HOME=/home/claude-worker/tools/jdk-21.0.5+11 \
  /home/claude-worker/tools/apache-maven-3.9.6/bin/mvn clean package
```
