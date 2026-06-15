#!/usr/bin/env bash
# Sobe o app web do Ouroboros numa porta so (8081): build + servidor com proxy
# para o gateway. Uso: ./mobile/start-web.sh   (depois abra http://localhost:8081)
set -e
cd "$(dirname "$0")"

PORT="${PORT:-8081}"

echo "==> Conferindo o gateway (porta 8080)..."
if ! curl -s -m 5 -o /dev/null http://localhost:8080/auth/login -X OPTIONS \
  -H 'Origin: http://localhost' -H 'Access-Control-Request-Method: POST'; then
  echo "!! Gateway nao respondeu em http://localhost:8080"
  echo "   Suba o backend antes:  docker compose -f infra/docker-compose.yml up -d"
  exit 1
fi
echo "   gateway OK"

echo "==> Instalando dependencias (se preciso)..."
[ -d node_modules ] || npm ci

echo "==> Gerando o build web..."
npm run build:web >/dev/null

echo "==> Liberando a porta $PORT..."
PID="$(ss -ltnp 2>/dev/null | grep ":$PORT " | grep -oP 'pid=\K[0-9]+' | head -1 || true)"
[ -n "$PID" ] && kill "$PID" 2>/dev/null && sleep 1 || true

echo "==> Iniciando o servidor..."
node app.web/serve.js "$PORT"
