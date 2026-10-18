"""Label stub for the OpenBoxes e2e stack (stdlib only).

:9100  raw TCP sink standing in for the Zebra printer (ZebraService.printDocument)
:8081  HTTP: POST anything = the labelary render (answers a 1x1 PNG, records the ZPL);
       GET /_printed and GET /_rendered = what was received (JSON lists, for the tests);
       POST /_reset clears both.
"""
import base64, json, socketserver, threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

PNG = base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==')
printed, rendered, lock = [], [], threading.Lock()


class Printer(socketserver.BaseRequestHandler):
    def handle(self):
        chunks = []
        while True:
            b = self.request.recv(65536)
            if not b:
                break
            chunks.append(b)
        with lock:
            printed.append(b''.join(chunks).decode('utf-8', 'replace'))


class Http(BaseHTTPRequestHandler):
    def _send(self, code, ctype, body):
        self.send_response(code)
        self.send_header('Content-Type', ctype)
        self.send_header('Content-Length', str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        with lock:
            data = {'/_printed': printed, '/_rendered': rendered}.get(self.path)
            body = json.dumps(data).encode() if data is not None else b''
        self._send(200 if data is not None else 404, 'application/json', body)

    def do_POST(self):
        n = int(self.headers.get('Content-Length') or 0)
        body = self.rfile.read(n).decode('utf-8', 'replace')
        if self.path == '/_reset':
            with lock:
                printed.clear(); rendered.clear()
            return self._send(204, 'text/plain', b'')
        with lock:
            rendered.append(body)
        self._send(200, 'image/png', PNG)

    def log_message(self, *a):
        pass


socketserver.ThreadingTCPServer.allow_reuse_address = True
threading.Thread(target=socketserver.ThreadingTCPServer(('0.0.0.0', 9100), Printer).serve_forever, daemon=True).start()
ThreadingHTTPServer(('0.0.0.0', 8081), Http).serve_forever()
