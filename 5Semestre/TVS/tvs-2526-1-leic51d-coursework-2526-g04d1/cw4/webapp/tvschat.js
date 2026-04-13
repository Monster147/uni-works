import os       from 'node:os';
import path     from 'node:path';
import express  from 'express';
import { execFile } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const DEFAULT_OLLAMA_URL   = 'http://127.0.0.1:11434';
const DEFAULT_OLLAMA_MODEL = 'llama3.2:1b';
const DEFAULT_MESSAGE      = 'Hint: sudo apt install fortune-mod fortunes';

const NODE_PORT    = process.env.NODE_PORT;
const OLLAMA_URL   = process.env.OLLAMA_URL   || DEFAULT_OLLAMA_URL;
const OLLAMA_MODEL = process.env.OLLAMA_MODEL || DEFAULT_OLLAMA_MODEL;

const __filename = fileURLToPath(import.meta.url);
const APP_DIR    = path.dirname(__filename);

///////////////////////////////////////////////////////
//
// Server: Listen on NODE_PORT and serve the homepage
//

if (!NODE_PORT) {
  process.stderr.write('ERROR: missing NODE_PORT configuration variable\n');
  process.exit(1);
}

const app = express();

// static assets (existing images + CSS + client JS)
app.use('/files', express.static(path.join(APP_DIR, 'files')));

// body parser for JSON
app.use(express.json());

// view engine (EJS)
app.set('views', path.join(APP_DIR, 'views'));
app.set('view engine', 'ejs');

// routes
app.get('/', getHomePage);
app.post('/chat', sendMessage);

app.listen(NODE_PORT, '0.0.0.0', () => {
  console.log(`[web] listening on http://localhost:${NODE_PORT}/`);
});

///////////////////////////////////////////////////////
//
// The homepage: 
//   - find Ollama's version
//   - inform about listening port and current count
//
async function getHomePage(req, res) {
  const host = os.hostname();
  const port = NODE_PORT;

  const [ollamaVersion, fortune] = await Promise.all([
    getOllamaVersion(),
    getFortune()
  ]);
  const message = fortune || DEFAULT_MESSAGE;

  res.render('home', {
    host,
    port,
    ollamaVersion,
    message
  });
}

///////////////////////////////////////////////////////
//
// Get Ollama version: 
//   - returns Ollama version
//   - or undefined, if Ollama is unavailable
//
async function getOllamaVersion() {
  try {
    const response = await fetch(`${OLLAMA_URL}/api/version`);
    const json = await response.json();
    return json.version;
  } catch (err) {
    console.error('[ollama]', err);
    return null;
  }
}

///////////////////////////////////////////////////////
//
// Get fortune: 
//   - runs 'fortune' and collects what gets written
//     to standard output
//
async function getFortune() {
  return new Promise((resolve) => {
    execFile(
      '/usr/games/fortune',
      ['-s'],
      { timeout: 1500 },
      (_error, stdout) => resolve(stdout)
    );
  });
}

///////////////////////////////////////////////////////
//
// POST /chat  –> proxy to Ollama /api/generate
// body: { message: string, context?: any }
//    - calls /api/generate (stream=false)
//    - returns { response, context }
//
async function sendMessage(req, res) {
  const { message, context } = req.body || {};
  const text = typeof message === 'string' ? message.trim() : '';

  if (!text) {
    return res.status(400).json({ error: 'missing message' });
  }

  const payload = {
    model: OLLAMA_MODEL,
    prompt: text,
    stream: false
  };
  if (context) payload.context = context;

  try {
    const r = await fetch(`${OLLAMA_URL}/api/generate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!r.ok) {
      const txt = await r.text().catch(() => '');
      return res
        .status(502)
        .json({ error: `ollama error ${r.status}`, details: txt.slice(0, 500) });
    }

    const j = await r.json();
    return res.json({
      response: j.response ?? '',
      context: j.context ?? null
    });
  } catch (err) {
    return res
      .status(504)
      .json({ error: 'ollama fetch error', details: String(err) });
  }
}

