#!/bin/bash
set -e

# Load env to display port
source /opt/gilmotech-brain/.env

echo "==> Building GilmoBrain..."
cd /opt/gilmotech-brain
JAVA_HOME=/home/claude-worker/tools/jdk-21.0.5+11 \
  /home/claude-worker/tools/apache-maven-3.9.6/bin/mvn clean package -DskipTests

echo "==> Copying JAR..."
cp target/brain.jar /opt/gilmotech-brain/brain.jar

echo "==> Restarting service..."
systemctl restart gilmotech-brain
sleep 2
systemctl status gilmotech-brain

echo "==> GilmoBrain deployed on port ${PORT_BRAIN}"
