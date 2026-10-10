#!/usr/bin/env python3
"""Paginated API -> idempotent JSONL sync with retries, state and resumability."""
import argparse
import json
import os
import random
import time
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.parse import urljoin, urlparse
from urllib.request import Request, urlopen


class SyncError(RuntimeError):
    pass


def fetch_json(url, token=None, retries=3, timeout=10, sleep=time.sleep):
    if urlparse(url).scheme not in {"http", "https"}:
        raise ValueError("API URL must use HTTP or HTTPS")
    headers = {"Accept": "application/json", "User-Agent": "FlowBridge-Demo/1.0"}
    if token:
        headers["Authorization"] = "Bearer " + token
    for attempt in range(retries + 1):
        try:
            with urlopen(Request(url, headers=headers), timeout=timeout) as response:
                return json.load(response)
        except HTTPError as e:
            if e.code not in (429, 500, 502, 503, 504) or attempt == retries:
                raise SyncError(f"API request failed: HTTP {e.code}") from e
        except (URLError, TimeoutError) as e:
            if attempt == retries:
                raise SyncError("API request failed after retries") from e
        sleep(min(2 ** attempt, 8) + random.uniform(0, .2))
    raise AssertionError("unreachable")


def ensure_same_origin(base_url, page_url):
    # Guard against malicious or accidental cross-origin pagination URLs,
    # especially when Authorization tokens may be present.
    base = urlparse(base_url)
    target = urlparse(page_url)
    if (base.scheme, base.netloc) != (target.scheme, target.netloc):
        raise SyncError("API pagination link crossed origin; refusing to forward credentials")


def sync(base_url, out, state_path, token=None, max_pages=100, fetch=fetch_json):
    """Each page is checkpointed after an atomic local write.

    Assumes source record IDs are stable; replacing changed records is idempotent.
    """
    out = Path(out)
    state_path = Path(state_path)
    out.parent.mkdir(parents=True, exist_ok=True)
    state_path.parent.mkdir(parents=True, exist_ok=True)
    existing = {}
    if out.exists():
        for line in out.read_text(encoding="utf-8").splitlines():
            if line.strip():
                item = json.loads(line)
                existing[str(item["id"])] = item
    state = json.loads(state_path.read_text(encoding="utf-8")) if state_path.exists() else {"next": base_url, "pages_completed": 0}
    url = state.get("next")
    pages = 0
    while url and pages < max_pages:
        ensure_same_origin(base_url, url)
        response = fetch(url, token=token)
        items = response.get("items")
        if not isinstance(items, list):
            raise SyncError("Expected a JSON object with items[]")
        for item in items:
            if not isinstance(item, dict) or "id" not in item:
                raise SyncError("Every item needs a stable id")
            existing[str(item["id"])] = item
        raw_next = response.get("next")
        next_url = urljoin(url, raw_next) if raw_next else None
        if next_url:
            ensure_same_origin(base_url, next_url)
        # atomic-ish local replacement protects previous results after a crash
        temp = out.with_suffix(out.suffix + ".tmp")
        with temp.open("w", encoding="utf-8") as f:
            for key in sorted(existing):
                f.write(json.dumps(existing[key], ensure_ascii=False, sort_keys=True) + "\n")
        temp.replace(out)
        state = {"next": next_url, "pages_completed": state.get("pages_completed", 0) + 1}
        temp_state = state_path.with_suffix(state_path.suffix + ".tmp")
        temp_state.write_text(json.dumps(state, indent=2), encoding="utf-8")
        temp_state.replace(state_path)
        url = next_url
        pages += 1
    return {"records": len(existing), "pages_this_run": pages, "complete": url is None, "state": str(state_path), "output": str(out)}


def main():
    p = argparse.ArgumentParser(description="Incrementally sync a paginated JSON REST API to JSONL")
    p.add_argument("url", help="API endpoint returning {items:[{id:...}],next:...}")
    p.add_argument("--output", default="outputs/records.jsonl")
    p.add_argument("--state", default="outputs/sync-state.json")
    p.add_argument("--max-pages", type=int, default=100)
    args = p.parse_args()
    if args.max_pages < 1:
        p.error("--max-pages must be >= 1")
    if urlparse(args.url).scheme == "http" and urlparse(args.url).hostname not in {"localhost", "127.0.0.1", "::1"}:
        p.error("For remote endpoints use HTTPS; HTTP is allowed only for the local demo")
    result = sync(args.url, args.output, args.state, token=os.environ.get("API_TOKEN"), max_pages=args.max_pages)
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
