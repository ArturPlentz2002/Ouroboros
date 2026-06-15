/**
 * Servidor do build web (app.web/dist) — UMA porta para tudo:
 *   - estatico (HTML/JS) com no-cache (evita bundle antigo preso no navegador)
 *   - proxy de /auth/* e /api/* para o gateway (default http://127.0.0.1:8080)
 *   - fallback SPA para as demais rotas
 *
 * Assim o navegador fala so com este servidor: funciona em localhost, no IP da
 * rede local e em qualquer dispositivo, sem CORS e sem expor a porta do gateway.
 *
 * Uso: node app.web/serve.js [porta]   (default 8081, escuta em 0.0.0.0)
 * Env: GATEWAY_URL (default http://127.0.0.1:8080)
 */
const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = Number(process.argv[2] || process.env.PORT || 8081);
const GATEWAY = new URL(process.env.GATEWAY_URL || 'http://127.0.0.1:8080');
const DIST = path.resolve(__dirname, 'dist');
const TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.map': 'application/json',
  '.ico': 'image/x-icon',
  '.txt': 'text/plain; charset=utf-8',
};

function proxy(req, res) {
  const upstream = http.request(
    {
      host: GATEWAY.hostname,
      port: GATEWAY.port || 80,
      path: req.url,
      method: req.method,
      headers: { ...req.headers, host: `${GATEWAY.hostname}:${GATEWAY.port}` },
    },
    (up) => {
      res.writeHead(up.statusCode || 502, up.headers);
      up.pipe(res);
    },
  );
  upstream.on('error', (e) => {
    res.writeHead(502, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ message: `gateway indisponivel: ${e.message}` }));
  });
  req.pipe(upstream);
}

function serveStatic(req, res) {
  const urlPath = decodeURIComponent((req.url || '/').split('?')[0]);
  const file = path.join(DIST, urlPath === '/' ? 'index.html' : urlPath);
  if (!file.startsWith(DIST)) {
    res.writeHead(403).end();
    return;
  }
  fs.readFile(file, (err, data) => {
    if (err) {
      fs.readFile(path.join(DIST, 'index.html'), (e2, html) => {
        if (e2) {
          res.writeHead(404).end('not found');
        } else {
          res.writeHead(200, { 'Content-Type': TYPES['.html'], 'Cache-Control': 'no-cache' });
          res.end(html);
        }
      });
      return;
    }
    res.writeHead(200, {
      'Content-Type': TYPES[path.extname(file)] || 'application/octet-stream',
      'Cache-Control': 'no-cache',
    });
    res.end(data);
  });
}

http
  .createServer((req, res) => {
    const p = req.url || '/';
    if (p.startsWith('/auth') || p.startsWith('/api') || p.startsWith('/oauth2')) {
      proxy(req, res);
    } else {
      serveStatic(req, res);
    }
  })
  .listen(PORT, '0.0.0.0', () => {
    console.log(`Ouroboros web em http://localhost:${PORT} (e no IP da LAN) — API via proxy para ${GATEWAY.origin}`);
  });
