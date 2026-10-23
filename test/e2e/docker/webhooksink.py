"""Webhook sink for the OpenBoxes e2e stack's `scenarios` profile (stdlib only).

:8082  HTTP: POST anything = an event delivery (openboxes.n8n.endpoint.url points at
       /webhook/events); every POST is recorded with its path, headers, body and arrival time and
       answered 200 with an empty JSON object (Content-Length or chunked bodies);
       GET /events?since=<n> = the recorded deliveries with seq > n (all of them without `since`),
       oldest first, as a JSON list of {seq, time, path, headers, body, json};
       DELETE /events clears the list (seq keeps counting, so a `since` taken before still works).
"""
import json, re, threading, time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlsplit

events, lock, counter = [], threading.Lock(), [0]
DIGITS, HEX = re.compile(rb'[0-9]+'), re.compile(rb'[0-9A-Fa-f]+')


class Http(BaseHTTPRequestHandler):
    def _send(self, code, body=b''):
        self.send_response(code)
        if code != 204:  # a 204 carries no body and no Content-Length
            self.send_header('Content-Type', 'application/json')
            self.send_header('Content-Length', str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _body(self):
        """The request body: Content-Length or chunked transfer coding; None when it is malformed."""
        if 'chunked' in (self.headers.get('Transfer-Encoding') or '').lower():
            chunks = []
            while True:
                size = self.rfile.readline().split(b';')[0].strip()
                if not HEX.fullmatch(size):
                    return None
                size = int(size, 16)
                if size == 0:
                    while self.rfile.readline() not in (b'\r\n', b'\n', b''):
                        pass  # trailers
                    return b''.join(chunks)
                chunk = self.rfile.read(size)
                if len(chunk) != size or self.rfile.readline() not in (b'\r\n', b'\n'):
                    return None
                chunks.append(chunk)
        n = (self.headers.get('Content-Length') or '0').strip().encode()
        if not DIGITS.fullmatch(n):
            return None
        n = int(n)
        body = self.rfile.read(n) if n else b''
        return body if len(body) == n else None

    def do_GET(self):
        url = urlsplit(self.path)
        if url.path != '/events':
            return self._send(404)
        try:
            since = int(parse_qs(url.query).get('since', ['0'])[0])
        except ValueError:
            return self._send(400, json.dumps({'error': 'since must be an integer'}).encode())
        with lock:
            body = json.dumps([e for e in events if e['seq'] > since]).encode()
        self._send(200, body)

    def do_DELETE(self):
        if urlsplit(self.path).path != '/events':
            return self._send(404)
        with lock:
            events.clear()
        self._send(204)

    def do_POST(self):
        raw = self._body()
        if raw is None:
            self.close_connection = True
            return self._send(400, json.dumps({'error': 'malformed request body'}).encode())
        body = raw.decode('utf-8', 'replace')
        try:
            parsed = json.loads(body) if body else None
        except ValueError:
            parsed = None
        with lock:
            counter[0] += 1
            events.append({'seq': counter[0], 'time': time.time(), 'path': self.path,
                           'headers': dict(self.headers.items()), 'body': body, 'json': parsed})
        self._send(200, b'{}')

    def log_message(self, *a):
        pass


ThreadingHTTPServer(('0.0.0.0', 8082), Http).serve_forever()
