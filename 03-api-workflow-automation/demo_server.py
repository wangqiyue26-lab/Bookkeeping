#!/usr/bin/env python3
"""Local-only synthetic paginated API; no external services needed."""
import json
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import urlparse, parse_qs

DATA = [
    {"id": "CRM-101", "customer": "Aspen Ltd", "status": "new", "value": 120},
    {"id": "CRM-102", "customer": "Northwind Ltd", "status": "qualified", "value": 300},
    {"id": "CRM-103", "customer": "Example Labs", "status": "new", "value": 80},
    {"id": "CRM-104", "customer": "Mariner Co", "status": "won", "value": 570},
    {"id": "CRM-105", "customer": "Beacon LLC", "status": "new", "value": 200},
]


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        parsed = urlparse(self.path)
        if parsed.path != "/api/leads":
            self.send_error(404)
            return
        page = max(1, int(parse_qs(parsed.query).get("page", ["1"])[0]))
        start = (page-1)*2
        results = {"items": DATA[start:start+2], "next": f"/api/leads?page={page+1}" if start+2 < len(DATA) else None}
        body = json.dumps(results).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


if __name__ == "__main__":
    print("Synthetic CRM running at http://127.0.0.1:8765/api/leads?page=1")
    HTTPServer(("127.0.0.1", 8765), Handler).serve_forever()
