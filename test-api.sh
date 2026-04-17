#!/bin/bash

TOKEN="02a6a705683c7622fe71632559b3ec8b0a03d32cb7a9fe9d2419f02d459fca51"
BASE_URL="https://brain.gilmotech.be"

echo "=== Test Health ==="
curl -s "$BASE_URL/api/health" \
  -H "X-Brain-Token: $TOKEN" | python3 -m json.tool

echo ""
echo "=== Test Météo Bruxelles ==="
curl -s -X POST "$BASE_URL/api/agent/run" \
  -H "Content-Type: application/json" \
  -H "X-Brain-Token: $TOKEN" \
  -d '{"task":"Quel temps fait-il a Bruxelles","sessionId":"test-001","mode":"conversation"}' | python3 -m json.tool
