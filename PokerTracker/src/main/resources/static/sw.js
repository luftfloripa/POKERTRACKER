const CACHE_NAME = 'pokertracker-v1';
const ASSETS_TO_CACHE = [
    '/',
    '/index.html',
    '/css/estilo.css',
    '/js/script.js',
    '/manifest.json'
];

self.addEventListener('install', event => {
    event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.addAll(ASSETS_TO_CACHE)));
    self.skipWaiting();
});

self.addEventListener('activate', event => {
    event.waitUntil(caches.keys().then(keys => Promise.all(keys.map(k => k !== CACHE_NAME ? caches.delete(k) : null))));
    self.clients.claim();
});

self.addEventListener('fetch', event => {
    const url = new URL(event.request.url);

    // 1. Se a requisição for para o Backend (API), ignore o SW e vá direto pro servidor
    if (url.origin !== location.origin || url.pathname.startsWith('/auth') || url.pathname.startsWith('/torneios')) {
        return;
    }

    // 2. Estratégia de Network First para navegação (index.html)
    if (event.request.mode === 'navigate') {
        event.respondWith(
            fetch(event.request).catch(() => caches.match('/index.html'))
        );
        return;
    }

    // 3. Estratégia Cache First para arquivos estáticos (CSS, JS, Imagens)
    event.respondWith(
        caches.match(event.request).then(response => {
            return response || fetch(event.request);
        })
    );
});